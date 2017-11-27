#!/bin/bash

if [ $# -lt 10 ]; then
        echo "Usage: $0 username password host database diagnosis-file question-file answer-file qa-link-file atgarder-file atgarder-link-file."
        exit 1
fi

USERNAME=$1
PASSWORD=$2
MYSQL_HOST=$3
DATABASE=$4

DIAGNOSIS_FILE=$5
TMP_DIAGNOSIS_FILE=$(mktemp)

QUESTION_FILE=$6
TMP_QUESTION_FILE=$(mktemp)

RESPONSE_FILE=$7
TMP_RESPONSE_FILE=$(mktemp)

LINK_FILE=$8
TMP_LINK_FILE=$(mktemp)

ATGARD_FILE=$9
ATGARD_LINK_FILE=${10}
TMP_MEASURE_FILE=$(mktemp)
TMP_RECOMMENDATION_FILE=$(mktemp)
TMP_MEASURE_PRIORITY_FILE=$(mktemp)

mysql -u $USERNAME -p$PASSWORD -h $MYSQL_HOST $DATABASE -e "truncate table prediction_priority; truncate table prediction_response; delete from prediction_question; delete from prediction_diagnosis; truncate measure_priority; delete from recommendation; delete from measure;"
cat $DIAGNOSIS_FILE | sed '1d' | awk 'BEGIN { FS="|" } {  print ""NR"|"$1"|"$2""}' > "$TMP_DIAGNOSIS_FILE"
mysql -u $USERNAME -p$PASSWORD $DATABASE --local-infile=1 -e "LOAD DATA LOCAL INFILE '$TMP_DIAGNOSIS_FILE' INTO TABLE prediction_diagnosis FIELDS TERMINATED BY '|';";

declare -A diagnosis_array
while read id diagnosis
do
    diagnosis_array["${diagnosis}"]=${id}
done < <(mysql -u $USERNAME -p$PASSWORD -h $MYSQL_HOST $DATABASE -se "select id, diagnosis_id from prediction_diagnosis")

cat $QUESTION_FILE | sed '1d' | awk 'BEGIN { FS="|" } {  print ""NR"|"$1"|"$2"|"$3"" }' > "$TMP_QUESTION_FILE"
mysql -u $USERNAME -p$PASSWORD -h $MYSQL_HOST $DATABASE --local-infile=1 -e "LOAD DATA LOCAL INFILE '$TMP_QUESTION_FILE' INTO TABLE prediction_question FIELDS TERMINATED BY '|' OPTIONALLY ENCLOSED BY '\"';"

declare -A prediction_array
while read id prediction
do
    prediction_array["${prediction}"]=${id}
done < <(mysql -u $USERNAME -p$PASSWORD -h $MYSQL_HOST $DATABASE -se "select id, prediction_id from prediction_question")

OLDIFS=$IFS
IFS="|"

while read index prediction_question answer_text prediction_answer is_default priority
do
    echo "$index|$answer_text|$prediction_answer|$is_default|$priority|${prediction_array["$prediction_question"]}"  >> "$TMP_RESPONSE_FILE"
done < <(cat $RESPONSE_FILE | sed '1d' | awk 'BEGIN { FS="|" } {  print ""NR"|"$1"|"$2"|"$3"|"$4"|"$5"|"$6"" }')

mysql -u $USERNAME -p$PASSWORD -h $MYSQL_HOST $DATABASE --local-infile=1 -e "LOAD DATA LOCAL INFILE '$TMP_RESPONSE_FILE' INTO TABLE prediction_response FIELDS TERMINATED BY '|' OPTIONALLY ENCLOSED BY '\"' (id,answer,prediction_id,@var1,priority,question_id) SET is_default = (@var1 = 'True');"

INDEX=1
while read diagnosis question_id priority
do
    echo "$INDEX|$priority|${diagnosis_array["$diagnosis"]}|${prediction_array["$question_id"]}" >> "$TMP_LINK_FILE"
    ((INDEX++))
done < <(cat $LINK_FILE | sed '1d' | sed 's/,/|/g')

mysql -u $USERNAME -p$PASSWORD -h $MYSQL_HOST $DATABASE --local-infile=1 -e "LOAD DATA LOCAL INFILE '$TMP_LINK_FILE' INTO TABLE prediction_priority FIELDS TERMINATED BY '|' OPTIONALLY ENCLOSED BY '\"';"

while read id type atgard
do
    echo "$id|$atgard|$type" >> "$TMP_RECOMMENDATION_FILE"
done < <(cat $ATGARD_FILE | sed '1,2d' | awk 'BEGIN { FS="|" } {  print ""$1"|"$2"|"$3"" }')

declare -A measure_diagnosis
DIAGNOS_INDEX=1
while read id diagnosis recommendation_id priority
do
    if [ -z "${measure_diagnosis["$diagnosis"]}" ]
    then
        measure_diagnosis["${diagnosis}"]=$DIAGNOSIS_INDEX
        echo "${DIAGNOSIS_INDEX}|${diagnosis}|\"\"|1.0" >> "$TMP_MEASURE_FILE"
        ((DIAGNOSIS_INDEX++))
    fi
    echo "$id|$priority|${measure_diagnosis["$diagnosis"]}|$recommendation_id" >> "$TMP_MEASURE_PRIORITY_FILE"
done < <(cat $ATGARD_LINK_FILE | sed '1,2d' | awk 'BEGIN { FS="|" } {  print ""NR"|"$1"|"$2"|"$3"" }')

mysql -u $USERNAME -p$PASSWORD -h $MYSQL_HOST $DATABASE --local-infile=1 -e "LOAD DATA LOCAL INFILE '$TMP_MEASURE_FILE' INTO TABLE measure FIELDS TERMINATED BY '|' OPTIONALLY ENCLOSED BY '\"';"
mysql -u $USERNAME -p$PASSWORD -h $MYSQL_HOST $DATABASE --local-infile=1 -e "LOAD DATA LOCAL INFILE '$TMP_RECOMMENDATION_FILE' INTO TABLE recommendation FIELDS TERMINATED BY '|' OPTIONALLY ENCLOSED BY '\"';"
mysql -u $USERNAME -p$PASSWORD -h $MYSQL_HOST $DATABASE --local-infile=1 -e "LOAD DATA LOCAL INFILE '$TMP_MEASURE_PRIORITY_FILE' INTO TABLE measure_priority FIELDS TERMINATED BY '|' OPTIONALLY ENCLOSED BY '\"';"

IFS=$OLDIFS
