#!/usr/bin/env tclsh

package require tdbc::postgres
package require csv

set dbUser "energyuser"
set dbPass "energy123"
set dbName "energydb"
set tableName "owid_energy"
set url "https://raw.githubusercontent.com/owid/energy-data/master/owid-energy-data.csv"
set filename "/tmp/owid-energy-data.csv"

# Downloading the CSV
puts "Downloading OWID dataset from GitHub"
exec curl -sL -o $filename $url
puts "Download complete"

puts "Connecting to database $dbName"

