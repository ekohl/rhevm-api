/*
 * Copyright © 2010 Red Hat, Inc.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.redhat.rhevm.api.powershell.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.Status;
import com.redhat.rhevm.api.powershell.util.ControllableExecutor;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPool;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;

import org.junit.runner.RunWith;

import static org.easymock.classextension.EasyMock.eq;
import static org.easymock.classextension.EasyMock.expect;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { PowerShellCmd.class })
@Ignore
public abstract class AbstractPowerShellResourceTest<R /* extends BaseResource */,
                                                     A /* extends AbstractPowerShellActionableResource<R> */>
    extends BasePowerShellResourceTest {

    protected final static String ASYNC_OPTION = " -async";
    protected final static String ASYNC_ENDING = " -async;";
    protected final static String ASYNC_TASKS =
        "$tasks = get-lastcommandtasks ;"
        + " if ($tasks) { $tasks ; get-tasksstatus -commandtaskidlist $tasks } ; ";

    protected A resource;
    protected ControllableExecutor executor;
    protected PowerShellPoolMap poolMap;
    protected PowerShellParser parser;
    protected PlaceHolderUriInfoProvider uriProvider;
    protected HttpHeaders httpHeaders;

    @Before
    public void setUp() throws Exception {
        executor = new ControllableExecutor();
        poolMap = createMock(PowerShellPoolMap.class);
        parser = PowerShellParser.newInstance();
        uriProvider = new PlaceHolderUriInfoProvider();
        httpHeaders = createMock(HttpHeaders.class);
        resource = getResource(executor, poolMap, parser, uriProvider);
    }

    @After
    public void tearDown() {
        verifyAll();
        uriProvider.setUriInfo(null);
    }

    protected void setUriInfo(UriInfo uriInfo) {
        uriProvider.setUriInfo(uriInfo);
    }

    protected Action getAction() {
        return getAction(false);
    }

    protected Action getAction(boolean async) {
        Action action = new Action();
        action.setId("56789");
        action.setAsync(async);
        return action;
    }

    protected UriInfo setUpActionExpectation(String baseUri, String verb, String command, Object ret) throws Exception {
        if (command != null) {
            mockStatic(PowerShellCmd.class);
            if (ret instanceof Throwable) {
                expect(PowerShellCmd.runCommand(setUpPoolExpectations(), command)).andThrow((Throwable)ret);
            } else  {
                expect(PowerShellCmd.runCommand(setUpPoolExpectations(), command)).andReturn((String)ret);
            }
        }

        UriInfo uriInfo = setUpBasicUriExpectations();
        if (baseUri != null) {
            expect(uriInfo.getPath()).andReturn(baseUri).anyTimes();
        }

        replayAll();

        return uriInfo;
    }

    protected void setUpHttpHeaderNullExpectations(String... names) {
        for (String name : names) {
            setUpHttpHeaderExpectations(name, null);
        }
    }

    protected void setUpHttpHeaderExpectations(String name, String value) {
        List<String> values = new ArrayList<String>();
        if (value != null) {
            values.add(value);
        }
        expect(httpHeaders.getRequestHeader(eq(name))).andReturn(values).anyTimes();
    }

    protected PowerShellPool setUpPoolExpectations() {
        return setUpPoolExpectations(1);
    }

    protected PowerShellPool setUpPoolExpectations(int times) {
        PowerShellPool pool = createMock(PowerShellPool.class);
        expect(poolMap.get()).andReturn(pool).times(times);
        return pool;
    }

    protected void verifyActionResponse(Response r, String baseUri, boolean async) throws Exception {
        verifyActionResponse(r, baseUri, async, null, null);
    }

    protected void verifyActionResponse(Response r, String baseUri, boolean async, String reason, String detailExerpt) throws Exception {
        assertEquals("unexpected status", async ? 202 : 200, r.getStatus());
        Object entity = r.getEntity();
        assertTrue("expect Action response entity", entity instanceof Action);
        Action action = (Action)entity;
        assertNotNull(action.getHref());
        assertNotNull(action.getId());
        assertNotNull(action.getLinks());
        assertEquals(async, action.isAsync());
        if (action.getStatus().equals(Status.FAILED.value()) && reason == null) {
            assertNotNull(action.getFault());
            System.out.println(action.getStatus() + " : " + action.getFault().getReason());
            System.out.println(action.getFault().getDetail());
        }
        assertTrue("unexpected status", async
                   ? action.getStatus().equals(Status.PENDING.value())
                     || action.getStatus().equals(Status.IN_PROGRESS.value())
                     || action.getStatus().equals(Status.COMPLETE.value())
                   : reason == null
                     ? action.getStatus().equals(Status.COMPLETE.value())
                     : action.getStatus().equals(Status.FAILED.value()));
        assertTrue(action.getLinks().size() == 2);
        assertEquals("expected parent link", "parent", action.getLinks().get(0).getRel());
        assertNotNull(action.getLinks().get(0).getHref());
        assertTrue(action.getLinks().get(0).getHref().indexOf(baseUri) != -1);
        assertNotNull(action.getLinks().get(1).getHref());
        assertEquals("expected replay link", "replay", action.getLinks().get(1).getRel());
        assertTrue(action.getLinks().get(1).getHref().indexOf(baseUri) != -1);
        assertEquals("unexpected async task", async ? 1 : 0, executor.taskCount());
        executor.runNext();
        if (reason != null) {
            assertNotNull(action.getFault());
            assertEquals(reason, action.getFault().getReason());
            assertTrue(action.getFault().getDetail().indexOf(detailExerpt) != -1);
        }
    }

    protected abstract A getResource(Executor executor, PowerShellPoolMap poolMap, PowerShellParser parser, UriInfoProvider uriProvider);
}
