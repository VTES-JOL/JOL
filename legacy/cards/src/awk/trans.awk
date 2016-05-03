
BEGIN { name="false" }

/^$/ { name="true" ; print "" ; next }

{ if (name == "true") { name="false" ; print "Name: "$0 } else { print $0 } }
