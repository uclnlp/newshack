Topic Finder
============

to run the server, do

```
   sbt 'run-main TopicFinderServer'
```

this should run the server on localhost:8888.

Currently the server accepts get requests like

```
   http://localhost:8888/find?id=http://dbpedia.org/resource/David_Cameron&id=http://dbpedia.org/resource/Barack_Obama
```

This returns related topics to both of the ids provided.



