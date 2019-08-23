prepare-abstracts:
	mvn -q exec:java -Dexec.mainClass=edu.cuhk.hccl.AppAbstracts \
		-Dexec.args="data/submissionInfo.xls 4 abstract-all-${title}.txt all ${title}"

abstract-wordcloud:
	cd word-cloud; rm -f *.csv
	mkdir -p topic-track/topics topic-track/verbose
	cd topic-track/topics; rm -f *.dat
	cd topic-track/verbose; rm -f *.out
	cd topic-track; rm -f *.txt
	make topic-model file=abstract-all-${title}.txt topics=${topics} \
		iterations=${iterations} keywords=${keywords} > abstract-all-${title}-${topics}.topics 2>&1
	sh topic-track-similarity.sh ${topics}
	mvn -q exec:java -Dexec.mainClass=edu.cuhk.hccl.AppAnalyzeSimilarity \
		-Dexec.args="topic-track/f1-${topics}.txt 0.0 matching-count.txt" > title-${title}/match-topics-${topics}.txt
	tar czf abstract-word-cloud-${topics}.tar.gz word-cloud/
	tar czf abstract-topic-track-${topics}.tar.gz topic-track/

topic-model:
	mvn -q exec:java -Dexec.mainClass=edu.cuhk.hccl.AppTopicModel \
		-Dexec.args="${file} ${topics} ${iterations} 10 ${keywords}"

track-keywords:
	mkdir -p topic-track/tracks
	cd topic-track/tracks; rm -f *.dat
	for i in 1 2 3 4 5 6 7 8 9 10 11 12 ; do \
		mvn -q exec:java -Dexec.mainClass=edu.cuhk.hccl.AppTrackWords \
			-Dexec.args="data/stopwords.txt data/tracks/$$i.txt topic-track/tracks/$$i.dat" ; \
    done

install-mallet:
	mvn install:install-file -Dfile=lib/mallet-2.0.8.jar -DgroupId=cc.mallet -DartifactId=mallet -Dversion=2.0.8 -Dpackaging=jar

tune-iterations:
	mkdir -p tune-iteration
	for (( i = 500; i <= 10000; i = i + 500)) ; do \
		mvn -q exec:java -Dexec.mainClass=edu.cuhk.hccl.AppEvaluateModel \
			-Dexec.args="abstract-all-true.txt 20 $$i 10 15" ; \
		mvn -q exec:java -Dexec.mainClass=edu.cuhk.hccl.AppTopicModel \
			-Dexec.args="abstract-all-true.txt 20 $$i 10 200" ; \
		sh topic-track-similarity.sh 20 ; \
		mvn -q exec:java -Dexec.mainClass=edu.cuhk.hccl.AppAnalyzeSimilarity \
			-Dexec.args="topic-track/f1-20.txt 0.0 tune-iteration-count.txt" > tune-iteration/match-topics-$$i.txt ; \
	done

# Example: make infer-topics modelfile=model-2014-20-5000.gz datafolder=dataset/tracks/interspeech
infer-topics:
	mvn -q exec:java -Dexec.mainClass=edu.cuhk.hccl.AppInferTrackTopics -Dexec.args="${modelfile} ${datafolder} 10000 5"
	
