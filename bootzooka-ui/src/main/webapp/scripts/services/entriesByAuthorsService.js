"use strict";

angular.module("smlBootzooka.entries").factory('EntriesByAuthorsService', function ($resource) {

    var self = this;

    var allAuthors = undefined;

    self.allUsersResource = $resource('/rest/users/all', { }, {
        get: {method: 'GET', isArray: true}
    });

    self.entriesByAuthorResource = $resource('/rest/entries/author/:authorId', { }, {
        get: {method: 'GET', isArray: true}
    });

    function callIfFunction(callback, parameter) {
        if (angular.isFunction(callback)) {
            callback(parameter);
        }
    }

    var entriesByAuthorsService = {};

    entriesByAuthorsService.getAllAuthors = function (successFunction) {
        if (angular.isUndefined(allAuthors)) {
            self.allUsersResource.get(null, function (data) {
                allAuthors = data;
                callIfFunction(successFunction, allAuthors);
            });
        } else {
            callIfFunction(successFunction, allAuthors);
        }
    };

    entriesByAuthorsService.loadAuthoredBy = function (authorId, successFunction) {
        self.entriesByAuthorResource.get({authorId: authorId}, successFunction);
    };

    return entriesByAuthorsService;
});