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

import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.common.util.ReflectionHelper;
import com.redhat.rhevm.api.model.CpuTopology;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.model.VMs;
import com.redhat.rhevm.api.resource.VmResource;
import com.redhat.rhevm.api.resource.VmsResource;
import com.redhat.rhevm.api.powershell.model.PowerShellVM;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

import static com.redhat.rhevm.api.common.util.CompletenessAssertor.validateParameters;
import static com.redhat.rhevm.api.powershell.util.PowerShellCmd.runCommand;

public class PowerShellVmsResource
    extends AbstractPowerShellCollectionResource<VM, PowerShellVmResource>
    implements VmsResource {

    public List<PowerShellVM> runAndParse(String command) {
        return PowerShellVmResource.runAndParse(getShell(), getParser(), command);
    }

    public PowerShellVM runAndParseSingle(String command) {
        return PowerShellVmResource.runAndParseSingle(getShell(), getParser(), command);
    }

    @Override
    public VMs list(UriInfo uriInfo) {
        VMs ret = new VMs();
        for (PowerShellVM vm : runAndParse(getSelectCommand("select-vm", uriInfo, VM.class))) {
            ret.getVMs().add(PowerShellVmResource.addLinks(vm));
        }
        return ret;
    }

    @Override
    public Response add(UriInfo uriInfo, VM vm) {
        validateParameters(vm, "name", "template.id|name", "cluster.id|name");
        StringBuilder buf = new StringBuilder();

        String templateArg = null;
        if (vm.getTemplate().isSetId()) {
            templateArg = PowerShellUtils.escape(vm.getTemplate().getId());
        } else {
            buf.append("$t = select-template -searchtext ");
            buf.append(PowerShellUtils.escape("name=" + vm.getTemplate().getName()));
            buf.append(";");
            templateArg = "$t.TemplateId";
        }

        String clusterArg = null;
        if (vm.getCluster().isSetId()) {
            clusterArg = PowerShellUtils.escape(vm.getCluster().getId());
        } else {
            buf.append("$c = select-cluster -searchtext ");
            buf.append(PowerShellUtils.escape("name=" +  vm.getCluster().getName()));
            buf.append(";");
            clusterArg = "$c.ClusterId";
        }

        buf.append("$templ = get-template -templateid " + templateArg + ";");

        buf.append("add-vm");

        buf.append(" -name " + PowerShellUtils.escape(vm.getName()) + "");
        if (vm.getDescription() != null) {
            buf.append(" -description " + PowerShellUtils.escape(vm.getDescription()));
        }
        buf.append(" -templateobject $templ");
        buf.append(" -hostclusterid " + clusterArg);
        if (vm.getType() != null) {
            buf.append(" -vmtype " + ReflectionHelper.capitalize(vm.getType().toString().toLowerCase()));
        }
        if (vm.isSetMemory()) {
            buf.append(" -memorysize " + Math.round((double)vm.getMemory()/(1024*1024)));
        }
        if (vm.getCpu() != null && vm.getCpu().getTopology() != null) {
            CpuTopology topology = vm.getCpu().getTopology();
            buf.append(" -numofsockets " + topology.getSockets());
            buf.append(" -numofcpuspersocket " + topology.getCores());
        }
        String bootSequence = PowerShellVM.buildBootSequence(vm.getOs());
        if (bootSequence != null) {
            buf.append(" -defaultbootsequence " + bootSequence);
        }

        PowerShellVM ret = runAndParseSingle(buf.toString());

        vm = PowerShellVmResource.addLinks(ret);

        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(ret.getId());

        return Response.created(uriBuilder.build()).entity(vm).build();
    }

    @Override
    public void remove(String id) {
        runCommand(getShell(), "remove-vm -vmid " + PowerShellUtils.escape(id));
        removeSubResource(id);
    }

    @Override
    public VmResource getVmSubResource(UriInfo uriInfo, String id) {
        return getSubResource(id);
    }

    @Override
    protected PowerShellVmResource createSubResource(String id) {
        return new PowerShellVmResource(id, getExecutor(), shellPools, getParser());
    }
}
