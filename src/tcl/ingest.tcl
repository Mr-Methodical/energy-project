#!/usr/bin/env tclsh

package require tdbc::postgres
package require csv

set dbUser "energyuser"
set dbPass "energy123"
set dbName "energydb"
set tableName "owid_energy"
set url "https://raw.githubusercontent.com/owid/energy-data/master/owid-energy-data.csv"
set filename "/tmp/owid-energy-data.csv"

puts "Downloading CSV: OWID dataset from GitHub"
exec curl -sL -o $filename $url
puts "Download complete"

puts "Connecting to database $dbName"
tdbc::postgres::connection create db \
    -user $dbUser -password $dbPass -db $dbName -host localhost -port 5432
puts "Connected!"

# creating headers so it has all our columns
puts "Reading CSV header"
set fp [open $filename r]
set headerLine [gets $fp]
set headers [::csv::split $headerLine]
puts "Read"

set colDefs {}
foreach h $headers {
    # any character not a letter, number, or underscore, we replace with underscore
    set clean [regsub -all {[^a-zA-Z0-9_]} $h "_"]
    lappend colDefs "\"$clean\" TEXT"
}

puts "Creating table '$tableName' and dropping current one if needed"
# send message to db to drop rows if any exists
# catch in case there are permissions issues or something else unexpected
catch { db allrows "DROP TABLE IF EXISTS $tableName" }
# creating columns in db
db allrows "CREATE TABLE $tableName ([join $colDefs {, }])"
puts "Table created"

set placeholders {}
for {set i 0} {$i < [llength $headers]} {incr i} {
    lappend placeholders ":val$i"
}
set insertSQL "INSERT INTO $tableName VALUES ([join $placeholders {, }])"
set stmt [db prepare $insertSQL]
# now stmt is connection ot the prepresed insertSQL in the database

puts "Inserting rows"
# none saved to disk until I tell it to:
db begintransaction
set count 0
while {[gets $fp line] >= 0} {
    if {[string trim $line] eq ""} continue
    set values [::csv::split $line]
    # filling in missing fields with postgres NULL values {}
    while {[llength $values] < [llength $headers]} {
        lappend values {}
    }
    set rowDict [dict create]
    for {set i 0} {$i < [llength $headers]} {incr i} {
        # get value from specific index
        set val [lindex $values $i]
        # if a field is empty change it to the postgres NULL {}
        if {$val eq ""} {
            set val {}
        }
        dict set rowDict "val$i" $val
    }
    #inserts into database (doesn't write yet only when we commit)
    $stmt execute $rowDict
    incr count
    if {$count % 5000 == 0} {
        puts "inserted $count rows"
    }
}
# save everything to disk
db commit
close $fp
puts "Done!! Inserted $count rows into '$tableName'"

# adding index on country and year because these will most likely be 
# most common lookups
puts "Adding index on country and year"
db allrows "CREATE INDEX idx_country_year ON $tableName (\"country\", \"year\")"

$stmt close
db close
puts "All done. Database is ready!"

