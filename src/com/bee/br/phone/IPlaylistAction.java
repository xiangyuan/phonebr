package com.bee.br.phone;

import com.bee.br.utils.TargetNotFoundException;


public interface IPlaylistAction extends ITargetAction {

	public IPlaylist[] getAllPlaylist();

	public IPlaylist[] writePlaylist(IPlaylist[] playlists)
			throws TargetNotFoundException;

	public boolean removePlaylist(IPlaylist[] playlists);

	public boolean updatePlaylist(IPlaylist[] playlists);
	
	int clearPlaylist();
}
