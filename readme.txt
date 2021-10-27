(using windows cmd curl)

//should build and run project on port 9000
sbt run

//Add new todo list
curl -X POST "localhost:9000/api/v1/todo-lists" -H  "accept: text/plain" -H  "Content-Type: application/json" -d "{\"listName\":\"a new list\"}"

//Add second todo list
curl -X POST "localhost:9000/api/v1/todo-lists" -H  "accept: text/plain" -H  "Content-Type: application/json" -d "{\"listName\":\"a second list\"}"

//rename second list
curl -X PUT "localhost:9000/api/v1/todo-lists/2" -H  "accept: text/plain" -H  "Content-Type: application/json" -d "{\"listName\":\"renamed\"}"

// delete second list
curl -X DELETE "localhost:9000/api/v1/todo-lists/2"

// add a task to first list
curl -X POST "localhost:9000/api/v1/todo-lists/1/tasks" -H  "accept: text/plain" -H  "Content-Type: application/json" -d "{\"title\":\"new todo\",\"priority\":1,\"completed\":false,\"dueDate\":\"2021-10-26T21:43-04:00\"}"

// update task on first list
curl -X PUT "localhost:9000/api/v1/todo-lists/1/tasks/1" -H  "accept: text/plain" -H  "Content-Type: application/json" -d "{\"completed\":true}"

// delete task on first list
curl -X DELETE "localhost:9000/api/v1/todo-lists/1/tasks/1"

// Get tasks:
http://localhost:9000/api/v1/todo-lists/1/tasks
    options arguments:
         priority (int, filter)
         completed (bool, filter)
         orderby (string, sort)
            values:
                due-date
                priority