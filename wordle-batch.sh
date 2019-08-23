infolder=$1
outfolder=$2
mkdir -p ${outfolder}
for csv in ${infolder}/*.csv
do
    echo "Processing ${csv} ...";
    filename=$(basename "${csv%.*}")
    tail -n +2 ${csv} | cut -d ',' -f1,3 --output-delimiter=':' > ${outfolder}/${filename}.probability
    tail -n +2 ${csv} | cut -d ',' -f1,2 --output-delimiter=':' > ${outfolder}/${filename}.count
done
