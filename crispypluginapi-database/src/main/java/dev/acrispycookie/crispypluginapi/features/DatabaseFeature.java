package dev.acrispycookie.crispypluginapi.features;

import dev.acrispycookie.crispycommons.logging.CrispyLogger;
import dev.acrispycookie.crispypluginapi.CrispyPluginAPI;
import dev.acrispycookie.crispypluginapi.features.options.PersistentOption;
import dev.acrispycookie.crispypluginapi.managers.HibernateDataManager;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public interface DatabaseFeature {

    CrispyPluginAPI getApi();
    String getName();

    default <D extends PersistentOption> void initDatabase(Set<D> data) {
        data.stream().map(D::clazz).forEach(getApi().getManager(HibernateDataManager.class)::registerAnnotated);
    }

    default boolean commitDataTransaction(Consumer<Session> consumer) {
        return commitDataTransaction(session -> {
            consumer.accept(session);
            return true;
        }) != null;
    }

    default <T> T commitDataTransaction(Function<Session, T> consumer) {
        HibernateDataManager manager = getApi().getManager(HibernateDataManager.class);
        Session session = null;
        Transaction transaction = null;
        try {
            session = manager.newSession();
            transaction = session.beginTransaction();
            T toReturn = consumer.apply(session);
            transaction.commit();
            session.close();
            return toReturn;
        } catch (Exception e) {
            CrispyLogger.printException(getApi().getPlugin(), e, "Couldn't complete a data transaction from the feature: " + getName());
            if (session != null) {
                if (transaction != null)
                    transaction.rollback();
                session.close();
            }
            return null;
        }
    }
}
