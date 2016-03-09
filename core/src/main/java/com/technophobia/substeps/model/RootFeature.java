/*
 *  Copyright Technophobia Ltd 2012
 *
 *   This file is part of Substeps.
 *
 *    Substeps is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Substeps is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with Substeps.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.technophobia.substeps.model;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * @author imoore
 */
public abstract class RootFeature {

    private String rawText;
    private Set<String> tags = Sets.newHashSet();


    public Set<String> getTags() {
        return tags;
    }


    public void setRawText(final String rawText) {
        this.rawText = rawText;
    }


    public void setTags(final Set<String> currentTags) {

        tags = currentTags;

    }


    public String getRawText() {
        return rawText;
    }
}
