
/^Name:/ { 
   print $0 > "nm.src"
   close("nm.src")
   system("sed 's/\\[...\\]//' nm.src | sed 's/\\[/ZZZZ/' > nm.dest")
   system("rm nm.src")
   getline < "nm.dest"
   close("nm.dest")
   echo $0
   print $0
   next }

{ print $0 }
