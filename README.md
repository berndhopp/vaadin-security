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

## visibility

Ilay-visibility is designed to be used in combination with a dependency-injection framework and with 
a one-class-per-component-pattern, although it can be used without that ( see ilay-visibility-manual ).

The first step is to decide for the integration you need:

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

The spring- and guice-integrations bring with them annotations called EnableIlay, that can be
attached to the GuiceVaadinServlet or SpringConfiguration, to bootstrap ilay-visibility within Vaadin.

### Guice

```java
    
    @EnableIlay
    //.. other annotations follow
    public class MyServlet extends GuiceVaadinServlet{
    }
```

### Spring

```java     
    @Configuration
    @EnableIlay
    public class MyConfiguration{
    }
```

Now bootstrap is finished, let's create a new component that should only be visible to admins.

We start with a VisibilityEvaluator, to decide whether a user is admin or not

```java
    public class IsAdminVisibilityEvaluator implements VisibilityEvalutor<VisibleForAdmins>{
        boolean evaluateVisibility(VisibleForAdmins annotation){
            User user = VaadinSession.getCurrent().getAttribute(User.class);
            
            return user != null && user.isAdmin();
        }
    }
```

Now the aforementioned annotation is needed, to connect the visibility-evaluator
to the components

```java
    @Retention(RetentionPolicy.RUNTIME)
    @Target(TYPE)
    @VisibilityAnnotation(IsAdminVisibilityEvaluator.class)
    public @interface VisibleForAdmins {
    }
```

And that annotation can now be attached to the components

```java
    @VisibleForAdmins
    public class AdminButton extends Button {
    }
```

On creation of the AdminButton, the DI-framework will run the connected VisibilityEvaluator 
and decide whether the button is visible or not. When permissions change, for example on login- or -out, 
in order for the visibility of your components to be re-evaluated, you need to call 

```java
    class AuthController {
    
    public void onLogin(){
        PermissionsChangedEvent.fire();
    }
}

```

### manual

If you use ilay-visibility-manual, you need to register your components, yes, manually

Either with a component without annotation:

```java
    public class MyView {
        public void foo(){
            Button adminButton = new Button("Admin", e -> doAdminThings());
            
            ManualVisibilityEvaluator isAdminEvaluator = new ManualVisibilityEvaluator(){
                 boolean evaluateVisibility(){
                     User user = VaadinSession.getCurrent().getAttribute(User.class);
                     
                     return user != null && user.isAdmin();
                 }
             };

            IlayVisibility.register(adminButton, isAdminEvaluator);
        }
    }
```

Or with AdminButton that we used before ( see above for the VisibleForAdmins-annotation )

```java
    @VisibleForAdmins
    public class AdminButton extends Button {
    }
        
     public class MyView {
         public void foo(){
             Button adminButton = new AdminButton();
             
             IlayVisibility.register(adminButton);
         }
     }   
```

# notes

We hope you enjoy working with ilay, if you have any suggestion, feel free to open
a github-issue or PR. 
