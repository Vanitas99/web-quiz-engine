trap "cleanup" 2

function cleanup() {
    echo "Trying to stop docker container $pid"
    docker stop "$pid"
}

echo "Starting maxima docker container"
pid=$(docker run -d -p 8080:8080 --rm --name maxima-backend mathinstitut/goemaxima:2021120900-latest)
echo "Docker running under pid: $pid"
echo "Starting Spring application"
gradle bootRun
cleanup

