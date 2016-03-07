
BEGIN { state="default" ; name="foo" }

/^Name:/ { state="name" ; name=$0 ; next }

{ if (state == "name") { state="default" ; print name" "$0 } else { print $0 } }
