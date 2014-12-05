package net.unicon.cas.addon.serviceregistry.raw.RawServiceRegistry;

import org.jasig.cas.services.AbstractRegisteredService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServiceRegistryDao;
import org.springframework.core.io.Resource;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class RawServiceRegistryDao implements ServiceRegistryDao {
    private final List<RegisteredService> registeredServices = new ArrayList<>();
    protected final Object lock = new Object();

    private Resource resource;

    @Override
    public RegisteredService save(RegisteredService registeredService) {
        synchronized (lock) {
            if (registeredService.getId() == RegisteredService.INITIAL_IDENTIFIER_VALUE) {
                ((AbstractRegisteredService)registeredService).setId(nextId());
            }
            this.registeredServices.remove(registeredService);
            this.registeredServices.add(registeredService);

            persistServices();

            return registeredService;
        }
    }

    @Override
    public boolean delete(RegisteredService registeredService) {
        synchronized (lock) {
            boolean happened = this.registeredServices.remove(registeredService);

            persistServices();

            return happened;
        }
    }

    @Override
    public List<RegisteredService> load() {
        synchronized (lock) {
            try {
                final FileInputStream fileInputStream = new FileInputStream(this.resource.getFile());
                final ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                List<RegisteredService> tRegisteredServices = (List<RegisteredService>) objectInputStream.readObject();
                objectInputStream.close();
                fileInputStream.close();
                return tRegisteredServices;
            } catch (IOException e) {
                // handle exception
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    @Override
    public RegisteredService findServiceById(long id) {
        synchronized (lock) {
            for (RegisteredService registeredService : this.registeredServices) {
                if (id == registeredService.getId()) {
                    return registeredService;
                }
            }
        }
        return null;
    }

    private synchronized void persistServices() {
        try {
            final FileOutputStream fileOutputStream = new FileOutputStream(this.resource.getFile());
            final ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(this.registeredServices);
            objectOutputStream.flush();
            objectOutputStream.close();

            fileOutputStream.flush();
            fileOutputStream.close();
        }
        catch (IOException e) {
            // handle the exception
        }
    }

    private synchronized long nextId() {
        long id = 0;

        for (RegisteredService registeredService : this.registeredServices) {
            if (registeredService.getId() > id) {
                id = registeredService.getId();
            }
        }

        return id;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }
}
