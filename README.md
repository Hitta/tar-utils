tar-utils
=========

This small tar library can be used to index tar files and create input streams of the individual files in the archive. This can be a neat way to avoid tar bombs in your runtime environment. Simply refuse to extract thousands of files to your poor servers! Keep them where they are manageable - in their archives!

For a tar archive with 100.000 entries, the index created will consume approximately 8MB memory.

Disk cache utilization hasn't been tested, but there is no reason to believe that the characteristics should be any different from individual files.


````
Note!
The maven project contains dependencies to a private repository.
To compile outside of hitta.se, remove these dependencies and replace with whatever is needed.
````
