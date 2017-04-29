# abstract

Unlike Spring Security or Apache Shiro, ILAY is tighly integrated into the workings of the Vaadin Framework 
and is thus a superior solution to any 'generic' authorization framework in many ways. 

The 3 subjects to authorization in Vaadin are components, views and items in a grid or list. 
ILAY enables you to restrict access to each of them with an easy yet powerful API. 

# building blocks

# permissions

since we are dealing with authorization, the most central concept is that of permissions. 
A permission can be put in front of a view for example, so that users that have that permission 
granted can navigate the view and for all others, the view does not exist (for all they can tell ).
 A permission in that context can be any java object for that there is an Authorizer, 
 a concept that we will see later. So to stay on the example for a second, let's assume the 
 permission that we want to put in front of a view named AdminView is the string "administrator", 
 so that any user that wants to see the AdminView needs to have the permission "administrator". That would be a one-liner:

```java

AdminView adminView = new AdminView();

Authorization.restrictView(adminView).to("administrator");

```

But where is the code that decides whether "administrator" is granted or not? That brings us to the next concept named authorizers.

# authorizers

Authorizer is a simple interface found [here](https://github.com/berndhopp/vaadin-security/tree/master/src/main/java/org/ilay/api/Authorizer.java). You 
will rarely implement Authorizer directly but in most cases use the [InMemoryAuthorizer](https://github.com/berndhopp/vaadin-security/tree/master/src/main/java/org/ilay/api/InMemoryAuthorizer.java). Only when data should be filtered on a backend/database level and verified on a frontend-level do you need to implement Authorizer directly, but that will be handled later. 

In our example, we decided that the permission needed is the string "administrator", so we need an authorizer-implementation for string-coded permissions, which could look like that:

```java
class StringPermissionAuthorizer implements InMemoryAuthorizer<String>{
    boolean isGranted(String permission){
        User user = VaadinSession.getCurrent().getAttribute(User.class);
        
        if("administrator".equals(permission)){
             return user.isAdmin();
        } else {
             //more logic
       }
    }

    public Class<String> getPermissionClass(){
        return String.class;
    }
}

```

The Authorizer needs to made available first of course, so the first thing to do is to call Authorization.start(). This method takes two overloads, one for Set<Authorizer> and one for Supplier<Set<Authorizer>>. A supplier is used when you want to have per-session Authorizers, if your Authorizers can be singleton you just use the simpler Set<Authorizer>-call. 

```java
class MyServlet extends VaadinServlet{

    @Override
    servletInitialized(){
        Authorizer<String> authorizer = new StringPermissionAuthorizer();

       Set<Authorizer> authorizers = new HashSet<>();

       authorizers.add(authorizer);
 
      Authorization.start(authorizers);
    }
}
```

# API

when you restric access to a view, you probably also don't want to show for example a button that will open the view ( which would not work in all cases of course ). So you may want to do something like this

```java
Authorization.restrictComponent(adminViewButton).to("administrator");
```

please note that after this call, you are not allowed to call Component.setVisible() as this would interfere with how ILAY works.

to restrict access to data, for example in a Grid, you can use the restrictData method

```java
Grid<Foo> myFooGrid = new Grid<Foo>();
Authorization.restrictData(Foo.class, myFooGrid);
```

Foo-instances are threaded as their own permissions, so there must be an Authorizer&lt;Foo&gt; at hand and only those instances of foo that return true for the authorizer's isGranted-method will be shown in the grid. Note that if you want to do filtering on the backend-level, you need to implement Authorizer directly ( instead of InMemoryAuthorizer ) and give a 'Filter' back, which can be for example a jpa/hibernate criterion and makes sure that only those datasets are returned from the backend which will then return true for isGranted(). 