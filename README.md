Lecko Analytics Add-on
=======

# Introduction
This document describe the installation procedure and configuratiojn for Lecko Addon 1.1.x. This version is compatible with eXo Platform 4.4.3 and superior.

Version 1.1.x is designed to make a full export.

Lecko Analytics (http://analytics.lecko.fr) integration for eXo Platform


# Installation
Addon can be installed with addon manager : in eXo Platform folder : 
    
    ./addon install exo-lecko
    
# Configuration
To configure Lecko addon, some properties can be added in gatein/conf/exo.properties :

Property | Description | Default Value
-------- | ----------- | -------------
exo.addons.lecko.job.start.expression | Export start time | 0 0 21 * * ?
exo.addons.lecko.job.stop.expression  | Export end time | 0 0 6 * * ?
exo.addons.lecko.job.enabled | Activate/deactivate addon | true
exo.addons.lecko.directory.out.name | Destination folder of the export | ${java.io.tmpdir}/lecko
exo.addons.lecko.out.name | Export filename | dump
exo.addons.lecko.SftpHost | Destination FTP hostname |
exo.addons.lecko.SftpUser | Destination FTP username |
exo.addons.lecko.SftpPassword | Destination FTP password |
exo.addons.lecko.SftPortNumber | Destination FTP port |
exo.addons.lecko.SftpRemotePath | Destination FTP path |
exo.addons.leckoSftp.ProxyAddress | Proxy hostname or IP | 
exo.addons.lecko.SftpProxyPort | Proxy Port |

# How it works
At start time, a job will be fired and will launch the export. This export will run until all necessary information exported or until endtime in parameters.

For each exported element (space stream or user stream), the service will mark the element as exported. Then, even if, we restart the platform, it knows which elements are already treaten and can continue where it stopped.

When the service reachs the end of the export, export file is sent to the parameterized FTP. In case of success, the service restarts his progression, delete the export file which was just sent, and stops. The next occurence will starts a new fresh export.

# MBEANS
Somes MBEANS for Lecko addon are availables. 
The mbeans is named exo:portal=portal,service=LeckoServiceController
Possible actions are :
- buildLeckoData : This action will start the data export. It will start even if exo.addons.lecko.job.enabled=false. It will do nothing if the export is already running.
- stopLeckoExport : this action will stop the export. It will do nothing if the export is not running
- UploadLeckoData : this action will upload the dump file to lecko's ftp. Do nothing if export is running, or if export is not finished
- enableLeckoJob(string) : set property exo.addons.lecko.job.enabled to true or false. Allow user to activate or deactivate jobs
- getEnableLeckoJob : to know if jobs are activated or not
- getJobStatus : to know where the export is. It can be Running or Stopped, and display the percentage of completion. Exmpl : "Export is stopped. Export is done at 88% for current extraction."
- resetExtraction : remove dump file (without sending it to lecko), and reset database. This can be used to start a fresh export.

# LOGS

## Info

Info logs display percentage of completion, with a line for each 5%. The log looks like :
    
    Lecko-Addons : Begin Extraction…
    Lecko-Addons : Begin Space Extraction…
    Extract Data from spaces 5%
    Extract Data from spaces 10%
    Extract Data from spaces 15%
    Extract Data from spaces 20%
    …
    Extract Data from spaces 95%
    
    Lecko-Addons : Begin User Extraction…
    Extract Data from users 5%
    Extract Data from users 10%
    …
    Extract Data from users 95%
    Lecko-Addons : End Extraction
    
## Debug
To activate debug level, you will have to add a line in conf/logback.xml, for class org.exoplatform.addons.lecko
With DEBUG log level, log will look like :

    Lecko-Addons : Begin Extraction…
    Lecko-Addons : Begin Space Extraction…
    Export datas for spaceId=xxx1
    Export datas for spaceId=xxx2
    ...
    Extract Data from spaces 5%
    …
    Extract Data from spaces 95%
    
    Lecko-Addons : Begin User Extraction…
    Extract Data from user:user1
    Extract Data from user:user2
    ...
    Extract Data from users 5%
    …
    Extract Data from users 95%
    Lecko-Addons : End Extraction
    
At DEBUG log level, all spaces and users identifiers are displayed.

## Possible errors
Sometimes, during the export, platform became unable to communicate with the database (IDM transaction problem  : [EXOGTN-2269](https://jira.exoplatform.org/browse/EXOGTN-2269)). If this type of exception is detected, export is stopped to not mark element treaten when they are not. The message in logs is :

    Lecko-Addons : Extraction stopped by ExportException. Stop the extract by security
    
with the stacktrace of the error.