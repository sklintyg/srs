# Intygstjänsten prestandatest

## Målmiljö
Primärt är det test och/eller QA-miljön hos Basefarm som prestandatesterna skall exekveras mot.
 
Sekundärt kan man mycket väl köra prestandatesterna mot lokal maskin för att identifiera hotspots eller problem i ett tidigt skede.

## Konfiguration och loggning
Om man vill finjustera hur gatling beter sig så finns följande konfigurationsfiler:

- src/test/resources/gatling.conf
- src/test/resources/logback-test.xml

Det man ofta vill komma åt är felloggar när ens tester börjar spruta ur sig 500 Server Error eller påstår att de inte kan parsa ut saker ur svaren. Öppna då logback-test.xml och kommentera in följande:

    <!-- Uncomment for logging ALL HTTP request and responses  -->
    <!-- <logger name="io.gatling.http" level="TRACE" />    -->
    <!-- Uncomment for logging ONLY FAILED HTTP request and responses -->
    <!-- <logger name="io.gatling.http" level="DEBUG" /> -->    
 
Som framgår ovan så kan man slå på antingen all HTTP eller enbart failade request/responses. Ovärderligt då Gatling inte ger särskilt mycket hjälp annat än HTTP Status när något går fel på servern. 

## Seedning av testdata
I mappen src/test/resources finns en csv-filer med testpersonnummer från skatteverket. (ca 16000 st)

### SOAP-request
I mappen src/test/resources/request-bodies finns xml-filer med SOAP-requests för de olika testerna. Dessa används som templates men innehåller till störst del statisk information som inte är av intresse för testerna.

## Hur startar jag en simulering

### Välj målmiljö
Ett alternativ är att öppna build.gradle och redigera gatlingBaseUrl i ext-blocket

- "http://localhost:8080"

Alternativt kan man ange -DbaseUrl=....... på kommandoraden.

### Exekvering från command-line
Testerna körs genom att ge:

- gradle gatling -DgatlingSimulation=TEST

där TEST är namnet på den test-class som ska exekveras.


## Hur följer jag upp utfallet?
Medan testet kör skriver Gatling ut lite progress-info på command-line men den ger ganska rudimentär information. Det intressanta är att titta på rapporterna som genereras efter att testerna slutförts. Dessa finns under mappen:

build/reports/gatling/results

Varje körning hamnar i en egen mapp, t.ex.:

build/reports/gatling/results/getriskpredictionforcertificate-1558101313283 etc

och har en index.html-fil där utfallet av simulationen redovisas.