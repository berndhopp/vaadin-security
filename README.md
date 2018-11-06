[![Published on Vaadin  Directory](https://img.shields.io/badge/Vaadin%20Directory-published-00b4f0.svg)](https://vaadin.com/directory/component/ilay---authorization-for-vaadin)
[![Stars on Vaadin Directory](https://img.shields.io/vaadin-directory/star/ilay---authorization-for-vaadin.svg)](https://vaadin.com/directory/component/ilay---authorization-for-vaadin)

# abstract

Ilay is a simple-to-use authentication-framework for Vaadin. It does not incorporate frameworks like Spring-Security or Apache Shiro, but
brings it's own api which is custom-tailored for the use with Vaadin. Ilay deals with navigation ( is the user allowed to see this view? ) 
and visibility ( should this component be visible to the user? ). 

## navigation

Ilay-navigation comes as a separate dependency:

```xml
    <dependency>
        <groupId>org.ilay</groupId>
        <artifactId>ilay-navigation</artifactId>
        <version>3.0.0</version>
    </dependency>
```

Please note that thanks to the new API's in Vaadin 10+, no bootstrapping-code is necessary.

Let's assume the case that a certain route-target is only to be accessed by users that have 
the role 'administrator'. Now the first step would be to define an AccessEvaluator, that 
determines whether or not the current user has that role or not. 

```java
class IsAdminAccessEvaluator implements AccessEvaluator {

    Supplier<UserRole> userRoleProvider;

    @Override
    public Access evaluate(Location location, Class<?> navigationTarget, Annotation annotation) {
        return UserRole.ADMIN.equals(userRoleProvider.get()) 
            ? Access.granted() 
            : Access.restricted(UserNotInRoleException.class);
    }
}
```

Note that an Access-instance is returned, not a boolean. Access has one granted()-method and
a lot of restricted()-methods, which reflect the different reroute-methods in BeforeEnterEvent,
to which they eventually delegate.

Now we need to connect the IsAdminAccessEvaluator to our view, the glue for this is an annotation
with a nice speaking name and an @NavigationAnnotation on it, that specifies the AccessEvaluator:

```java
    @NavigationAnnotation(IsAdminAccessEvaluator.class)
    @Retention(RetentionPolicy.RUNTIME)
    public interface OnlyForAdmins {
    }
```

OnlyForAdmins can then be used to prevent users that are not admins from entering the protected views

```java
    @OnlyForAdmins
    @Route("admin-view")
     public class AdminView extends Div {
     }
```

And that's it. Now users without Admin-permission will be redirected to an error-view. 

Some readers may have noticed that the AccessEvaluator has a generic type-parameter for the
annotation it is attached to. So if we like to have additional information in there, like:

```java
    @NavigationAnnotation(IsAdminAccessEvaluator.class)
    @Retention(RetentionPolicy.RUNTIME)
    public interface OnlyForAdmins {
        String someAdditionalInfo();
    }
```
 
We could change IsAdminAccessEvaluator to

```java
class IsAdminAccessEvaluator implements AccessEvaluator<OnlyForAdmins> {

    Supplier<UserRole> userRoleProvider;

    @Override
    public Access evaluate(Location location, Class<?> navigationTarget, OnlyForAdmins annotation) {
        
        Log.info(annotation.someAdditionalInformation());
        
        return UserRole.ADMIN.equals(userRoleProvider.get()) 
            ? Access.granted() 
            : Access.restricted(UserNotInRoleException.class);
    }
}
```

##visibility

visibility needs to be integrated into your cdi-framework of use in order to work, so the first step is 
to decide for the integration you need:

for Spring:

```xml
    <dependency>
        <groupId>org.ilay</groupId>
        <artifactId>ilay-navigation-spring</artifactId>
        <version>3.0.0</version>
    </dependency>
```

for Guice:

```xml
    <dependency>
        <groupId>org.ilay</groupId>
        <artifactId>ilay-navigation-guice</artifactId>
        <version>3.0.0</version>
    </dependency>
```

for manual ( no di-framework )

```xml
    <dependency>
        <groupId>org.ilay</groupId>
        <artifactId>ilay-navigation-manual</artifactId>
        <version>3.0.0</version>
    </dependency>
```