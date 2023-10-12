/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.fabcar;

import java.util.Objects;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import com.owlike.genson.annotation.JsonProperty;

@DataType()
public final class HashFile {

    @Property()
    private final String filename;

    @Property()
    private final String username;

    @Property()
    private final String filehash;

    @Property()
    private final String filedate;

    public String getfilename() {
        return filename;
    }

    public String getusername() {
        return username;
    }

    public String getfilehash() {
        return filehash;
    }

    public String getfiledate() {
        return filedate;
    }

    public HashFile(@JsonProperty("filename") final String filename, @JsonProperty("username") final String username,
                    @JsonProperty("filehash") final String filehash, @JsonProperty("filedate") final String filedate) {
        this.filename = filename;
        this.username = username;
        this.filehash = filehash;
        this.filedate = filedate;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        HashFile other = (HashFile) obj;

        return Objects.deepEquals(new String[] {getfilename(), getusername(), getfilehash(), getfiledate()},
                new String[] {other.getfilename(), other.getusername(), other.getfilehash(), other.getfiledate()});
    }

    @Override
    public int hashCode() {
        return Objects.hash(getfilename(), getusername(), getfilehash(), getfiledate());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode())
                + " [filename=" + filename + ", username="
                + username + ", filehash=" + filehash + ", filedate=" + filedate + "]";
    }
}
