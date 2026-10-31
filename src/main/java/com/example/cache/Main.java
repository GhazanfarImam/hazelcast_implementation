
package com.example.cache;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

public class Main {

    public static void main(String[] args) {

        // Step 1: Start Hazelcast inside this application
        Config config = new Config();
        config.setClusterName("dev");

        // Disable multicast discovery for this local demo
        config.getNetworkConfig()
                .getJoin()
                .getMulticastConfig()
                .setEnabled(false);

        HazelcastInstance hazelcast =
                Hazelcast.newHazelcastInstance(config);

        EntityManagerFactory emf = null;

        try {
            // Step 2: Initialize JPA and Hibernate
            emf = Persistence.createEntityManagerFactory("hazelcast-jpa");

            Long employeeId;

            // Step 3: Insert employee
            EntityManager em = emf.createEntityManager();

            try {
                EntityTransaction tx = em.getTransaction();
                tx.begin();

                Employee employee =
                        new Employee("Rahul", "Backend Development");

                em.persist(employee);
                tx.commit();

                employeeId = employee.getId();
                System.out.println("Inserted employee ID: " + employeeId);
            } finally {
                em.close();
            }

            // Step 4: Evict entity from second-level cache
            emf.getCache().evict(Employee.class, employeeId);

            // Step 5: First lookup
            EntityManager em1 = emf.createEntityManager();

            try {
                System.out.println("\n--- First lookup ---");
                Employee first = em1.find(Employee.class, employeeId);
                System.out.println(first);
            } finally {
                em1.close();
            }

            // Step 6: Second lookup
            EntityManager em2 = emf.createEntityManager();

            try {
                System.out.println("\n--- Second lookup ---");
                Employee second = em2.find(Employee.class, employeeId);
                System.out.println(second);
            } finally {
                em2.close();
            }

        } finally {
            if (emf != null) {
                emf.close();
            }

            // Stop the embedded Hazelcast member
            hazelcast.shutdown();
        }
    }
}
