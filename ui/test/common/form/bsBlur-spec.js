describe('Blur directive', function () {

  var scope, form, elm, $httpBackend;

  beforeEach(angular.mock.module('smlBootzooka'));

  beforeEach(angular.mock.inject(function ($rootScope, $compile, _$httpBackend_) {
    $httpBackend = _$httpBackend_;
    $httpBackend.expectGET('api/users').respond('any');
    elm = angular.element(
      '<form name="form"><input type="text" ng-model="model.login" name="login" bs-blur="changeLogin()"></form>');
    scope = $rootScope;

    scope.model = {login: ''};
    scope.callSpy = 'not called';
    scope.changeLogin = function () {
      scope.callSpy = 'called';
    };
    $compile(elm)(scope);
    scope.$digest();
    form = scope.form;
    $httpBackend.flush();
  }));

  it('should not call bound function for different events', function () {
    // when
    elm.find('input').trigger('dummyEvent');
    // then
    expect(scope.callSpy).toBe('not called');
  });

  it('should execute given function on focus out', function () {
    // when
    elm.find('input').trigger('blur');
    // then
    expect(scope.callSpy).toBe('called');
  });

});
