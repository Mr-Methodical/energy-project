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
tdbc::postgres::connection create db -user $dbUser -password $dbPass -db $dbName
puts "Connected!"

# creating headers so it has all our columns
puts "Reading CSV header"
set fp [open $filename r]
set headerLine [get $fp]
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
db allrows "CREATE TABLE $tablename ([join $colDefs {, }])"
puts "Table created"

set placeholders {}
for {set i 0} {$i < [llength $headers]} {incr i} {
    lappend placeholders ":val$i"
}
set insertSQL "INSERT INTO $tableName VALUES ([join $placeholders {, }])"
set stmt [db prepare $insertSQL]
# now stmt is connection ot the prapresed insertSQL in the database

puts "Inserting rows"
db 

