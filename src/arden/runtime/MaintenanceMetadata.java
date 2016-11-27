// arden2bytecode
// Copyright (c) 2010, Daniel Grunwald
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification, are
// permitted provided that the following conditions are met:
//
// - Redistributions of source code must retain the above copyright notice, this list
//   of conditions and the following disclaimer.
//
// - Redistributions in binary form must reproduce the above copyright notice, this list
//   of conditions and the following disclaimer in the documentation and/or other materials
//   provided with the distribution.
//
// - Neither the name of the owner nor the names of its contributors may be used to
//   endorse or promote products derived from this software without specific prior written
//   permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS &AS IS& AND ANY EXPRESS
// OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
// AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
// IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
// OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package arden.runtime;

import java.util.Date;

public class MaintenanceMetadata {
	private String title;
	private String mlmName;
	private ArdenVersion ardenVersion;
	private String version;
	private String institution;
	private String author;
	private String specialist;
	private Date date;
	private Validation validation;
	
	public MaintenanceMetadata() {
		
	}
	
	public MaintenanceMetadata(String title, String mlmName, ArdenVersion ardenVersion, String version, String institution,
			String author, String specialist, Date date, Validation validation) {
		this.title = title;
		this.mlmName = mlmName;
		this.ardenVersion = ardenVersion;
		this.version = version;
		this.institution = institution;
		this.author = author;
		this.specialist = specialist;
		this.date = date;
		this.validation = validation;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setMlmName(String mlmName) {
		this.mlmName = mlmName;
	}

	public String getMlmName() {
		return mlmName;
	}

	public void setArdenVersion(ArdenVersion ardenVersion) {
		this.ardenVersion = ardenVersion;
	}

	public ArdenVersion getArdenVersion() {
		return ardenVersion;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getVersion() {
		return version;
	}

	public void setInstitution(String institution) {
		this.institution = institution;
	}

	public String getInstitution() {
		return institution;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getAuthor() {
		return author;
	}

	public void setSpecialist(String specialist) {
		this.specialist = specialist;
	}

	public String getSpecialist() {
		return specialist;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Date getDate() {
		return date;
	}

	public void setValidation(Validation validation) {
		this.validation = validation;
	}

	public Validation getValidation() {
		return validation;
	}

	public enum Validation {
		EXPIRED,
		TESTING,
		RESEARCH,
		PRODUCTION
	}

	public enum ArdenVersion implements Comparable<ArdenVersion> {
		V1(1, 0), // ASTM E1460-1992
		V2(2, 0),
		V2_1(2, 1),
		V2_5(2, 5);
		// not supported yet:
		// V2_6(2, 6),
		// V2_7(2, 7),
		// V2_8(2, 8),
		// V2_9(2, 9),
		// V2_10(2, 10);

		public final int major;
		public final int minor;

		private ArdenVersion(int major, int minor) {
			this.major = major;
			this.minor = minor;
		}

		@Override
		public String toString() {
			// e.g. "Version 2.5"
			StringBuilder versionBuilder = new StringBuilder("Version ");
			versionBuilder.append(major);
			if (minor != 0) {
				versionBuilder.append('.');
				versionBuilder.append(minor);
			}
			return versionBuilder.toString();
		}
	}

}
