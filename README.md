# Web Quiz Engine
A simple engine for creating and passing quizzes through HTTP API.

## Operations and their results

### Create a new quiz

To create a new quiz, you need to send a json with the four keys: `title`, `text`, `options` and `answer`. 
At this moment, all these keys are not required.

An example:

```
curl -X POST -H "Content-Type: application/json" -d '{"title":"The Java Logo", "text":"What is depicted on the Java logo?", "options": ["Robot", "Tea leaf", "Cup of coffee", "Bug"], "answer": 2}' http://localhost:8888/api/quizzes
```

The result contains the same json with `id`:
```
{"id":1,"title":"The Java Logo","text":"What is depicted on the Java logo?","options":["Robot","Tea leaf","Cup of coffee","Bug"],"answer":2}
```

### Get a quiz

To get an info about a quiz, you need to specify its `id` in url.

```
curl -v -X GET http://localhost:8888/api/quizzes/1
```

The result does not contain `answer`.
```
{"id":1,"title":"The Java Logo","text":"What is depicted on the Java logo?","options":["Robot","Tea leaf","Cup of coffee","Bug"]}
```

If the specified quiz does not exist, the server returns `HTTP 404`.