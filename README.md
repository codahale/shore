Shore
=====

*What makes Jersey fun.*

Shore is a web application framework which ties together
[Jersey](https://jersey.dev.java.net/), [Jetty](http://www.mortbay.org/jetty/), 
[Hibernate](https://www.hibernate.org/), and 
[Guice](http://code.google.com/p/google-guice/) into an awesome foundation for 
data-backed, RESTful web services in Java.

How To Write A Shore Application
--------------------------------

### Step 1: Write an entity class

    @Entity
    @Table(name="widgets")
    public class Widget {
      @Id
      @GeneratedValue(strategy=GenerationType.AUTO)
      private Integer id;
  
      @Column(name="name")
      private String name;
  
      @Column(name="description")
      private String description;
  
      public Integer getId() { return id; }
  
      public String getName() { return name; }
      public void setName(String name) { this.name = name; }
  
      public String getDescription() { return description; }
      public void setDescription(String description) { this.description = description; }
    }

### Step 2: Write a Data Access Object (DAO)

First the interface:
    
    public interface WidgetDAO {
      public abstract Widget findByName(String name);
      public abstract Widget save(Widget widget);
    }

Then the implementation:
    
    public class WidgetDAOImpl extends AbstractDAO implements WidgetDAO {
      
      @Inject
      public WidgetDAOImpl(Provider<Session> provider) {
        super(provider);
      }

      @Transactional
      @Override
      public Widget findByName(String name) {
        final Criteria criteria = currentSession().createCriteria(Widget.class);
        criteria.add(Restrictions.eq("name", name));
        criteria.setMaxResults(1);
        return (Widget) criteria.uniqueResult();
      }

      @Transactional
      @Override
      public Widget save(Widget widget) {
        currentSession().save(widget);
        return widget;
      }
      
    }
    
Then associate the DAO interface with the implementation:
    
    @ImplementedBy(WidgetDAOImpl.class)
    public interface WidgetDAO {
      // ...
    }

### Step 3: Write a resource
    
    @Path("/widget/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public class WidgetResource {
      private final WidgetDAO widgetDAO;

      @Inject
      public WidgetResource(WidgetDAO widgetDAO) {
        this.widgetDAO = widgetDAO;
      }

      @GET
      @Transactional
      public String getWidget(@PathParam("name") String name) {
        final Widget widget = widgetDAO.findByName(name);

        if (widget != null) {
          return "[Widget name:" + widget.getName() + ", description:" + widget.getDescription() + "]";
        }

        throw new WebApplicationException(Response.status(Status.NOT_FOUND).build());
      }

      @POST
      @Transactional
      public Response makeWidget(@Context UriInfo uriInfo,
          @PathParam("name") String name,
          String description) {

        final Widget widget = new Widget();
        widget.setName(name);
        widget.setDescription(description);
        widgetDAO.save(widget);

        return Response.created(uriInfo.getBaseUriBuilder().path(WidgetResource.class).build(name)).build();
      }
    }

### Step 4: Write a configuration
    
    public class WidgetApiConfig extends AbstractConfiguration {
      @Override
      protected void configure() {
        addResourcePackage("<name of the package where WidgetResource is>");
        addEntityPackage("<name of the package where Widget is>");
      }

      @Override
      public String getExecutableName() {
        return "widget-api";
      }
    }

### Step 5: Write a main class
    
    public class WidgetAPI {
      public static void main(String[] args) throws Exception {
        Shore.run(new WidgetApiConfig(), args);
      }
    }


### Step 6: Configure the connection:

Add some configuration details to `development.properties`:
    
    hibernate.connection.username=sa
    hibernate.connection.password=
    hibernate.connection.pool_size=1
    hibernate.dialect=org.hibernate.dialect.HSQLDialect
    hibernate.connection.url=jdbc:hsqldb:mem:WidgetDB
    hibernate.connection.driver_class=org.hsqldb.jdbcDriver
    hibernate.hbm2ddl.auto=create-drop

(This assumes you're using the [HSQLDB](http://hsqldb.org/) drivers.)

### Step 7: Run it!

By executing the main class (`WidgetAPI`, in this case) you can:

#### Generate a drop-and-create SQL schema script
    
    java -jar widget-api.jar schema --config=development.properties

This doesn't actually modify the database -- it just outputs the SQL to the
console. You can copy and paste it into your database or pipe it into a
database client process once you've verified the SQL.

#### Generate a migration SQL schema script
    
    java -jar widget-api.jar schema --config=development.properties --migration

This generates a non-destructive migration to bring the database schema up to 
date. **N.B.:** It's not perfect -- you may need to make some changes yourself.

#### Run an HTTP server
    
    java -jar widget-api.jar server --config=development.properties --port=8080

And use it:
    
    curl http://0.0.0.0:8080/widgets/hoople
    curl --data-binary="A hoople." http://0.0.0.0:8080/widgets/hoople
    curl http://0.0.0.0:8080/widgets/hoople
