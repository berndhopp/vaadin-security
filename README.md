# abstract

Ilay is a pretty simple authentication-framework based on navigation-restriction. Ilay does
not change the visibility of components or filter data in Grids, it merely restricts 
navigation between route-targets. 

# building blocks

Take for example the case that a certain route-target
is only to be accessed by users that have the role 'administrator'. Now the first step would be
to create an annotation called VisibleTo and annotate it with RestrictionAnnotation

```java
    @RestrictionAnnotation(RoleBasedAccessEvaluator.class)
    public interface VisibleTo {
        UserRole value();
    }
```

The RoleBasedAccessEvaluator is an AccessEvaluator that could look something like the
following. Note that the generic type for this AccessEvaluator is the type of the annotation
and the annotation is the last parameter of 'evaluate'.

```java
class RoleBasedAccessEvaluator implements AccessEvaluator<VisibleTo> {

    Supplier<UserRole> userRoleProvider;

    @Override
    public Access evaluate(Location location, Class<?> navigationTarget, VisibleTo annotation) {
        final boolean hasRole = annotation.value().equals(userRoleProvider.get());

        return hasRole ? Access.granted() : Access.restricted(UserNotInRoleException.class);
    }
}
```

VisibleTo can then be used to prevent users that don't have the required role to enter
the route-target by just annotating the respective class

```java
    @Route("adminview")
    @VisibleTo(UserRole.Admin)
     public class AdminView extends Div {
     }
```