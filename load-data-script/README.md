# Bootstrap SRS-data

We will receive data from SRS-project in form of either csv-files or excel-files. Our script requires input to be csv-files with pipe as field separator so the first step is to convert the excel-files. To do this you can either use save as in editor of choice for these documents or use a command such as `ssconvert` (`ssconvert` is available in `gnumeric` project for Arch linux).

The input-files required are:
* diagnosis.csv (diagnosis supported by SRS together with prevalence)
* questions.csv
* answers.csv
* qa-link.csv
* atgarder.csv
* atgarder-link.csv

## Running the script

To run the script execute it as follows
```
./load-data.sh <mysql-username> <mysql-password> <mysql-host> <mysql-database> <diagnosis-file> <question-file> <answer-file> <qa-link-file> <atgarder-file> <atgarder-link-file>
```

As an example you can use provided input files and test run the script with the following command
```
./load-data.sh <mysql-username> <mysql-password> localhost srs example-input/diagnosis.csv example-input/questions.csv example-input/answers.csv example-input/qa-link.csv example-input/atgarder.csv example-input/atgarder-link.csv
```

This requires read and write access to where the system writes temporary files.

## Cleaning data

The input files are required to not include any new line feeds other than those used to mark a new row of data. Clean the input by removing these new line feeds.

Please also make sure that the files have correct line terminators (not CLRF). This is easily remedied with `dos2linux`.
