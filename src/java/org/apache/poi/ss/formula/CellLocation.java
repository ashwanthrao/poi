/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.ss.formula;

/**
 * Stores the parameters that identify the evaluation of one cell.<br/>
 */
final class CellLocation {
	public static final CellLocation[] EMPTY_ARRAY = { };
	
	private final int _sheetIndex;
	private final int _rowIndex;
	private final int _columnIndex;
	private final int _hashCode;

	public CellLocation(int sheetIndex, int rowIndex, int columnIndex) {
		if (sheetIndex < 0) {
			throw new IllegalArgumentException("sheetIndex must not be negative");
		}
		_sheetIndex = sheetIndex;
		_rowIndex = rowIndex;
		_columnIndex = columnIndex;
		_hashCode = sheetIndex + 17 * (rowIndex + 17 * columnIndex);
	}
	public int getSheetIndex() {
		return _sheetIndex;
	}
	public int getRowIndex() {
		return _rowIndex;
	}
	public int getColumnIndex() {
		return _columnIndex;
	}

	public boolean equals(Object obj) {
		CellLocation other = (CellLocation) obj;
		if (getSheetIndex() != other.getSheetIndex()) {
			return false;
		}
		if (getRowIndex() != other.getRowIndex()) {
			return false;
		}
		if (getColumnIndex() != other.getColumnIndex()) {
			return false;
		}
		return true;
	}
	public int hashCode() {
		return _hashCode;
	}

	/**
	 * @return human readable string for debug purposes
	 */
	public String formatAsString() {
		return  "ShIx=" + getSheetIndex() + " R=" + getRowIndex() + " C=" + getColumnIndex();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer(64);
		sb.append(getClass().getName()).append(" [");
		sb.append(formatAsString());
		sb.append("]");
		return sb.toString();
	}
}