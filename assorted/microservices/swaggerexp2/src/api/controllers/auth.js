/**
 * Created by joaovieg on 29/11/16.
 */
var Auth = function(){

    var validate = function(req, res, next) {
        req.validation = true;
        next();
    };

    return { validate: validate };
}();

module.exports = Auth;