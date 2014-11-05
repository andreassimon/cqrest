Release Process
===============

 - Merge feature branches
 - Remove `-SNAPSHOT` suffix from version number
 - Extend [CHANGES.md](CHANGES.md)
 - Change repository key to `libs-release-local`
 - Commit with message `Release 0.1.1`
 - Tag the commit `v0.1.1`
 - Push master and tags
 - Publish the libraries to Artifactory

New Feature Branches
====================
 - Change repository key to `libs-snapshot-local`
 - Commit with message `#23 Created feature branch` where `#23` is the issue number of the new feature
