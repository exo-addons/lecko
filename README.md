Lecko Analytics Add-on
=======

Lecko Analytics (http://analytics.lecko.fr) integration for eXo Platform

Some jobs are added to be able to stop and resume the export. The first job is StartLeckoExportJob, which create a new thread which resume the export. The second job is StopLeckoExportJob. This job will stop the export. The idea is to make the export running during the night to not impact platform performances.
Then, when the extract is finished, we send datas to lecko FTP, and the current status is reseted for the next run.

Theses jobs can be deactivated with property exo.addons.lecko.job.enabled. If set to false, startJob, stopJob and exportJob will have no effect.

Properties :
* exo.addons.lecko.job.start.expression=0 0 21 * * ? #when export starts. Default value : start export each days at 9:00 PM
* exo.addons.lecko.job.stop.expression=0 0 6 * * ? #when export stops. Default value : stop export each days at 6:00 AM
* exo.addons.lecko.job.enabled=true #enable jobs
* exo.addons.lecko.out.name=mytestDump #file name 
* exo.addons.lecko.directory.out.name=./temp/lecko #folder name in which the file will be exported. Default value : JVM temp folder

* exo.addons.lecko.SftpHost=ftphostName
* exo.addons.lecko.SftpUser=ftpUser
* exo.addons.lecko.SftpPassword=ftppassword
* exo.addons.lecko.SftPortNumber=22
* exo.addons.lecko.SftpRemotePath=ftpremotePath


Some actions are available with MBeans :
- buildLeckoData : This action will start the data export. It will start even if exo.addons.lecko.job.enabled=false. It will do nothing if the export is already running.
- stopLeckoExport : this action will stop the export. It will do nothing if the export is not running
- UploadLeckoData : this action will upload the dump file to lecko's ftp. Do nothing if export is running, or if export is not finished
- enableLeckoJob(string) : set property exo.addons.lecko.job.enabled to true or false. Allow user to activate or deactivate jobs
- getEnableLeckoJob : to know if jobs are activated or not
- getJobStatus : to know where the export is. It can be Running or Stopped, and display the percentage of completion. Exmpl : "Export is stopped. Export is done at 88% for current extraction."
- resetExtraction : remove dump file (without sending it to lecko), and reset database. This can be used to start a fresh export.

