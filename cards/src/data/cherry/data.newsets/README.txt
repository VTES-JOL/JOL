
How to incorporate a new cardset:

Download the cared list, name it <setname>.base
(http://www.white-wolf.com/vtes/downloads/cardlist.txt)

addZZZZ <setname>

If you need to download from: (name it <setname>.txt)
http://www.white-wolf.com/vtes/index.php?line=cardlist.html
then you need to run addZZZZfromHtml <setname>
instead.

genprop <setname> <setabbreviation> <jolprefix>

Check the checklist from ww site to add (advanced) to appropriate vamps

Append the list of id mappings to the card map. 
cat <setname.map> >> base.prop

Make sure base.prop has no spaces ending lines

In vi, replace ZZZZ with ctrl-M [ to put the set classification on the
line after the name.  Save this version off as <setname>.base

(run cleansecards <setname> to fix formatting problems in card list and to copy to base.txt)
copy base.txt and base.prop into the right location

run JolAdmin main to see if there are any cards without ids.

sed out the id= in card.map to make card.test

run card.test through the deck creator to see if there are any errors recognizing cards.


CHECK THAT Cardtype: is on its own line

*****Old Directions*********
(  run addZZZZ <setname>, creating <setname>.zzz
*deprecated* Go to the ww website, html card texts version, select all, paste it to a
wordpad, save it under name <setname>.txt  Make sure there are newlines
at beginning and end of the file.

Run the trans.awk script on it to add Name:

Run the set.awk field to add the ZZZZ before the set name.
)

*deprecated* In vi, replace ZZZZ with ctrl-M [ to put the set classification on the
line after the name.  Save this version off as Cardlist.txt
(now <setname>.base)


On the file with the ZZZZ, grep for the new set, to get a file with just Name: and set designations.  sed out the Name: and the set desginations to get a list of card names for the new set.  Use awk '{print ++i"="$0}' to make ids for all these cards.
(or just run genprop <setname> <setabbreviation> <jolprefix> )

Append this list of id mappings to the card map.

(run cleansecards <setname> to fix formatting problems in card list and to copy to base.txt)
copy base.txt and base.map into the right location

Make sure base.map has no spaces ending lines

run JolAdmin main to see if there are any cards without ids.

sed out the id= in card.map to make card.test

run card.test through the deck creator to see if there are any errors recognizing cards.
