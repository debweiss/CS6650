package cs6650App.app.src.main.java;

import cs6650App.rest.SimpleGetPostResource;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

// root resource and provider classes
public class Assignment1App extends Application {

    private Set<Object> singletons = new HashSet<Object>();

    public Assignment1App() {

        singletons.add(new SimpleGetPostResource());
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }

}


