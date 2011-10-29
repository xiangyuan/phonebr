package com.bee.br.phone;

import com.bee.br.utils.TargetNotFoundException;


public interface IContactGroupAction extends ITargetAction {
	public IContactGroup[] getContactGroups();

	public IContactGroup[] addContactGroup(IContactGroup[] groups)
			throws TargetNotFoundException;

	public boolean updateContactGroup(IContactGroup[] groups);

	public boolean removeContactGroup(IContactGroup[] groups);
}
