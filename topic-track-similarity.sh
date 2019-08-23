let "topics=$1-1" # index from 0
tracks=12
PROJECT=$PWD
mkdir -p $PROJECT/topic-track/similarity
mkdir -p $PROJECT/topic-track/verbose
cd $PROJECT/topic-track/similarity; rm -f *.out
cd $PROJECT/topic-track/verbose; rm -f *.out
cd $PROJECT/topic-track/topics; rm -f *.dat
for i in `seq 1 $tracks`
do
    for j in `seq 0 $topics`
    do
        cut -d ',' -f1 $PROJECT/word-cloud/topic_words$j.csv | sed 1d > $PROJECT/topic-track/topics/$j.dat
        cd $PROJECT; mvn -q exec:java -Dexec.mainClass=edu.cuhk.hccl.AppComputeFMeasure -Dexec.args="$PROJECT/word-cloud/topic_words$j.csv $PROJECT/topic-track/tracks/$i.dat true $PROJECT/topic-track/similarity/$j.out $PROJECT/topic-track/verbose/topic$j-track$i.out"
        #perl $PROJECT/Text-Similarity-0.10/bin/text_similarity.pl --type=Text::Similarity::Overlaps $PROJECT/topic-track/topics/$j.dat $PROJECT/topic-track/tracks/$i.dat >> $PROJECT/topic-track/similarity/$j.out
    	#perl $PROJECT/Text-Similarity-0.10/bin/text_similarity.pl --verbose --type=Text::Similarity::Overlaps $PROJECT/topic-track/topics/$j.dat $PROJECT/topic-track/tracks/$i.dat > $PROJECT/topic-track/verbose/topic$j-track$i.out 2>&1
    done
done
cd $PROJECT/topic-track/similarity/; ls -v ./ | xargs paste > $PROJECT/topic-track/f1-$1.txt
