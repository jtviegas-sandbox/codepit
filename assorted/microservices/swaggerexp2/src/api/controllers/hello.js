'use strict';
/*
 'use strict' is not required but helpful for turning syntactical errors into true errors in the program flow
 https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Strict_mode
*/

/*
 Modules make it possible to import JavaScript files into your application.  Modules are imported
 using 'require' statements that give you a reference to the module.

  It is a good idea to list the modules that your application depends on in the package.json in the project root
 */
var util = require('util');

/*
 Once you 'require' a module you can reference the things that it exports.  These are defined in module.exports.

 For a controller in a127 (which this is) you should export the functions referenced in your Swagger document by name.

 Either:
  - The HTTP Verb of the corresponding operation (get, put, post, delete, etc)
  - Or the operationId associated with the operation in your Swagger document

  In the starter/skeleton project the 'get' operation on the '/hello' path has an operationId named 'hello'.  Here,
  we specify that in the exports of this module that 'hello' maps to the function named 'hello'
 */


var Hello = function(){

    var getTwo = function(req, res, next) {
        var id = req.swagger.params.id.value;

        var result = {"ok": true, "result": {"id": id}};
        if(true == req.validation)
            result.validation = true;
        res.json(result);
    };

    var getOne = function(req, res, next) {
        var id = req.swagger.params.id.value;
        var result = {"ok": true, "result": {"id": id}};
        if(true == req.validation)
            result.validation = true;
        res.json(result);
    };

    var del = function(req, res, next) {
        var id = req.swagger.params.id.value;
        var result = {"ok": true, "result": {"id": id}};
        if(true == req.validation)
            result.validation = true;
        res.json(result);
    };

    var post = function(req, res, next) {
        var msg = req.body;
        var result = {"ok": true, "result": {"msg": msg}};
        if(true == req.validation)
            result.validation = true;
        res.json(result);
    };

    return { getTwo:getTwo, getOne:getOne, del:del, post:post };
}();

module.exports = Hello;



