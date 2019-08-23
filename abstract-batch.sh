mkdir -p title-$1
mkdir -p word-cloud; mkdir -p topic-track/topics; mkdir -p topic-track/verbose;
make track-keywords
rm -f matching-count.txt # remove this file for large runs to store all matching counts
make prepare-abstracts title=$1
for run in {1..3}
do
    for topic in {10..20}
    do
        for number in 50 100 150 200
        do
            make abstract-wordcloud title=$1 topics=${topic} iterations=10000 keywords=${number}
            storedir=abstract-title-$1-topics-${topic}-keywords-${number}
            mkdir -p ${storedir}
            mv title-$1/match-topics-${topic}.txt ${storedir}/
            mv probability-space-${topic}.txt ${storedir}/
            mv *.tar.gz ${storedir}/
        done
    done
    mkdir -p abstract-run-${run}
    mv abstract-title-true-topics-* abstract-run-${run}
done
