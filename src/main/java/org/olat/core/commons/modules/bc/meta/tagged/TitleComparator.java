/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.commons.modules.bc.meta.tagged;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import org.olat.core.commons.modules.bc.meta.MetaInfo;
import org.olat.core.commons.modules.bc.meta.MetaTitleComparator;
import org.olat.core.util.vfs.VFSItem;

/**
 * Compare the title or the filename
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class TitleComparator implements Comparator<VFSItem> {
	private final Collator collator;
	private final MetaTitleComparator comparator;

	public TitleComparator(Collator collator) {
		this.collator = collator;
		comparator = new MetaTitleComparator(collator);
	}

	public TitleComparator(Locale locale) {
		this(Collator.getInstance(locale));
	}

	public int compare(VFSItem i1, VFSItem i2) {
		if(i1 instanceof MetaTagged && i2 instanceof MetaTagged) {
			MetaInfo m1 = ((MetaTagged)i1).getMetaInfo();
			MetaInfo m2 = ((MetaTagged)i2).getMetaInfo();
			return comparator.compare(m1, m2);
		}
		
		String t1 = i1.getName();
		String t2 = i2.getName();
		return collator.compare(t1, t2);
	}
}
