package dev.acrispycookie.crispypluginapi.features;

import dev.acrispycookie.crispycommons.logging.CrispyLogger;
import dev.acrispycookie.crispypluginapi.CrispyPluginAPI;
import dev.acrispycookie.crispypluginapi.features.options.ConfigurationOption;
import dev.acrispycookie.crispypluginapi.features.options.PersistentOption;
import dev.acrispycookie.crispypluginapi.features.options.StringOption;
import dev.acrispycookie.crispypluginapi.managers.HibernateDataManager;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class DatabaseFeature<C extends ConfigurationOption, M extends StringOption, P extends StringOption, D extends PersistentOption> extends CrispyFeature<C, M, P, D> {

    public DatabaseFeature(CrispyPluginAPI api) {
        super(api);
        getData().stream().map(D::clazz).forEach(api.getManager(HibernateDataManager.class)::registerAnnotated);
    }

    @Override
    public <T> T getData(D option, Class<T> clazz, Object id) {
        return commitDataTransaction(session -> {
            return session.get(clazz, id);
        });
    }

    public boolean commitDataTransaction(Consumer<Session> consumer) {
        return commitDataTransaction(session -> {
            consumer.accept(session);
            return true;
        }) != null;
    }

    public <T> T commitDataTransaction(Function<Session, T> consumer) {
        HibernateDataManager manager = api.getManager(HibernateDataManager.class);
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
            CrispyLogger.printException(api.getPlugin(), e, "Couldn't complete a data transaction from the feature: " + getName());
            if (session != null) {
                if (transaction != null)
                    transaction.rollback();
                session.close();
            }
            return null;
        }
    }
}
