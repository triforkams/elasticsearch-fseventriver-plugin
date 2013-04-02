ElasticSearch File System Event River plugin
============================================

The file system event river plugin allows you to listen for file system events from a configurable location, process the create, modify and delete events and index any files found through these events.


Pre-requisites
--------------

The plug-in is tested with version 0.20.5 of ElasticSearch. It may work with earlier or later versions.

The use of Java 7 is required. Earlier versions of Java will not work due to the use of various I/O related service introduced in Java 7.

The [Attachment Plugin](https://github.com/elasticsearch/elasticsearch-mapper-attachments) should be installed as well.


Getting Started
===============

For now, installation requires you to check out the code and run 'mvn package' yourself after which the fseventriver.jar and commons-io.jar need to be copied to the elastic search lib directory.


Configuration
-------------

Creating the index and river data can be done using the following command:

```sh
curl -XPUT localhost:9200/_river/fsevent_river/_meta -d '
{
    "type" : "fsevent",
    "fs_event" : {
        "name" : "some name",
        "url" : "/tmp/test"
    },
    "index" : {
        "index" : "name_of_the_index",
        "type" : "doc",
        "bulk_size" : 100
    }
}'
```

All fields are optional and if not present a default will be used.

The name is a free format text with which one can identify the particular configuration.

The url is the string representing the location on disk to watch. This must be an absolute path. The content of this string is OS dependant.

The index contains the name of the index to use. This is a string, but may not contain any whitespace. If the index does not exist, it will be created once the configuration has been parsed for the first time.

The bulk size, the number of index requests queued for processing, defaults to 100 but is configurable. The flush interval, after which any queued request will be indexed regardless of reaching the bulk size, is not configurable yet. It is set to 30 seconds


At present, new index configuration is picked up pretty much immediately, updates to the configuration require a restart.


Limitations
-----------

Since this plug-in relies on events to tell it of changes to the file system, this plug-in cannot index something that was already there before it started watching the file system.
One way to work around that would be to combine this plug-in with the [FileStem River](https://github.com/dadoonet/fsriver/) plug-in.

For listening to the file system events, we rely on the WatchService interface from Java 7. This in turn relies on the underlying implementations to actually propagate any events to its subscribers. Some file systems, particularly the distributed ones, may not support an event mechanism or are too limited to be of use. Those can therefor not be indexed using this plug-in.


Future ideas
------------

The plug-in was created as part of a PoC. It is therefor limited in scope and options.
Some ideas about extending this plug-in include:

- allow the flush interval to be configured
- allow 'includes' and/or 'excludes' to be configured in order to limit what will be indexed
- make it configurable to watch a url recursively
