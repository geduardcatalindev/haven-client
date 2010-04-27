/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven;

import java.util.*;

public class GameUI extends Widget {
    static final Coord meterc = new Coord(95, 10);
    public final int plid;
    public final Charinfo chr;
    SlenHud slen;
    MapView map;
    Fightview frl;
    Cal cal;
    Map<Integer, Widget> meters = new TreeMap<Integer, Widget>();
    
    static {
	addtype("gameui", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    String chrnm = (String)args[0];
		    int plid = (Integer)args[1];
		    return(new GameUI(parent, chrnm, plid));
		}
	    });
    }
    
    public GameUI(Widget parent, String chrnm, int plid) {
	super(Coord.z, new Coord(800, 600), parent);
	this.chr = new Charinfo(ui.sess.glob, chrnm);
	this.plid = plid;
	slen = new SlenHud(Coord.z, this);
	new Bufflist(new Coord(95, 50), this, this.chr);
	new Avaview(new Coord(10, 10), this, plid);
	cal = new Cal(new Coord(333, 10), this);
    }
    
    private void setfight(Fightview frl) {
	if((this.frl == null) && (frl != null)) {
	    ui.destroy(this.cal);
	    this.cal = null;
	} else if((this.frl != null) && (frl == null)) {
	    this.cal = new Cal(new Coord(333, 10), this);
	}
	this.frl = frl;
    }

    public void dstchild(Widget wdg) {
	if(wdg == frl) {
	    setfight(null);
	}
    }

    public Widget makechild(WidgetFactory f, Object[] pargs, Object[] cargs) {
	String place = ((String)pargs[0]).intern();
	if(place == "map") {
	    Coord off = (Coord)cargs[0];
	    Widget w = f.create(Coord.z, this, new Object[] {sz, off, plid});
	    w.lower();
	    return(this.map = (MapView)w);
	} else if(place == "plist") {
	    return(f.create(new Coord(10, 100), this, new Object[] {plid}));
	} else if(place == "wnd") {
	    return(f.create(Coord.z, slen, cargs));
	} else if(place == "amenu") {
	    return(f.create(new Coord(670, 5), slen, cargs));
	} else if(place == "minimap") {
	    int id = (Integer)cargs[0];
	    return(f.create(new Coord(5, 5), slen, new Object[] {new Coord(125, 125), id}));
	} else if(place == "frlist") {
	    Fightview frl = (Fightview)f.create(new Coord(790, 100), this, cargs);
	    setfight(frl);
	    return(frl);
	} else if(place == "meter") {
	    String meter = (String)pargs[1];
	    int slot;
	    if(meter.equals("hp")) {
		slot = 0;
	    } else if(meter.equals("stam")) {
		slot = 2;
	    } else if(meter.equals("speed")) {
		slot = 3;
	    } else if(meter.equals("glut")) {
		slot = 4;
	    } else {
		for(slot = 0; true; slot++) {
		    if((slot == 0) || (slot == 2) || (slot == 3) || (slot == 4))
			continue;
		    if(!meters.containsKey(slot))
			break;
		}
	    }
	    Widget w = f.create(meterc.add((slot / 2) * 65, (slot % 2) * 20), this, cargs);
	    meters.put(slot, w);
	    return(w);
	}
	throw(new UI.UIException("Illegal gameui placement", place, pargs));
    }

    public void uimsg(String msg, Object... args) {
	if(msg == "err") {
	    slen.error((String)args[0]);
	} else if(msg == "setbelt") {
	    int slot = (Integer)args[0];
	    if(args.length < 2) {
		slen.setbelt(slot, null);
	    } else {
		slen.setbelt(slot, ui.sess.getres((Integer)args[1]));
	    }
	} else if(msg == "chr") {
	    String an = ((String)args[0]).intern();
	    if(an == "pag") {
		chr.paginaemsg(Utils.splice(args, 1));
	    } else if(an == "at") {
		chr.cattrmsg(Utils.splice(args, 1));
	    } else if(an == "prt") {
		chr.party.msg(Utils.splice(args, 1));
	    } else if(an == "buff") {
		chr.buffmsg(Utils.splice(args, 1));
	    }
	} else {
	    super.uimsg(msg, args);
	}
    }

    public void wdgmsg(Widget sender, String msg, Object... args) {
	if(sender == slen) {
	    super.wdgmsg(this, msg, args);
	    return;
	}
	super.wdgmsg(sender, msg, args);
    }
    
    public static Charinfo getchr(Widget wdg) {
	GameUI gi = wdg.findparent(GameUI.class);
	if(gi == null)
	    return(null);
	return(gi.chr);
    }
}
