package net.kunmc.lab.internalkpm.upgrader.mocks;

import lombok.AllArgsConstructor;
import org.kunlab.kpm.interfaces.KPMRegistry;
import org.kunlab.kpm.interfaces.hook.HookExecutor;
import org.kunlab.kpm.interfaces.hook.HookRecipientList;
import org.kunlab.kpm.interfaces.hook.KPMHook;
import org.kunlab.kpm.interfaces.hook.KPMHookRecipient;

@AllArgsConstructor
public class HookExecutorMock implements HookExecutor
{
    private final KPMRegistry registry;

    @Override
    public void runHook(KPMHookRecipient recipient, KPMHook hook)
    {

    }

    @Override
    public void runHook(HookRecipientList recipients, KPMHook hook)
    {

    }

    @Override
    public KPMRegistry getRegistry()
    {
        return this.registry;
    }
}
