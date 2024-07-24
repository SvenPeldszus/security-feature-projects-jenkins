package jenkins.security;

import org.gravity.security.annotations.requirements.Integrity;
import org.jenkinsci.remoting.Role;

/**
 * Predefined {@link Role}s in Jenkins.
 *
 * <p>
 * In Jenkins, there is really only one interesting role, which is the Jenkins master.
 * Agents, CLI, and Maven processes are all going to load classes from the master,
 * which means it accepts anything that the master asks for, and thus they need
 * not have any role.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.587 / 1.580.1
 */
// &begin[feat_Roles]
public class Roles {
    /**
     * Indicates that a callable runs on masters, requested by agents/CLI/maven/whatever.
     */
	@Integrity
    public static final Role MASTER = new Role("master");

    /**
     * Indicates that a callable is meant to run on agents.
     *
     * This isn't used to reject callables to run on the agent, but rather to allow
     * the master to promptly reject callables that are really not meant to be run on
     * the master (as opposed to ones that do not have that information, which gets
     * {@link Role#UNKNOWN})
     */
	@Integrity
    public static final Role SLAVE = new Role("slave");

    private Roles() {}
}
// &end[feat_Roles]