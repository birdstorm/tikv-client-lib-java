/*
 *
 * Copyright 2017 PingCAP, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.pingcap.tikv.types;

import static com.pingcap.tikv.types.TimestampType.fromPackedLong;
import static com.pingcap.tikv.types.TimestampType.toPackedLong;

import com.pingcap.tikv.codec.CodecDataInput;
import com.pingcap.tikv.codec.CodecDataOutput;
import com.pingcap.tikv.exception.TiClientInternalException;
import com.pingcap.tikv.meta.TiColumnInfo;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.sql.Date;

public class DateType extends DataType {
  static DateType of(int tp) {
    return new DateType(tp);
  }
  private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
  private static final IntegerType codecObject = IntegerType.DEF_LONG_LONG_TYPE;

  private DateType(int tp) {
    super(tp);
  }

  @Override
  public Object decodeNotNull(int flag, CodecDataInput cdi) {
    long val = IntegerType.decodeNotNullPrimitive(flag, cdi);
    LocalDateTime localDateTime = fromPackedLong(val);
    if (localDateTime == null) {
      return null;
    }
    //TODO revisit this later.
    return new Date(localDateTime.getYear() - 1900,
        localDateTime.getMonthValue() - 1,
        localDateTime.getDayOfMonth());
  }

  @Override
  public void encodeNotNull(CodecDataOutput cdo, EncodeType encodeType, Object value) {
    Date in;
    try {
      if (value instanceof Date) {
        in = (Date) value;
      } else {
        // format ensure only date part without time
        in = new Date(format.parse(value.toString()).getTime());
      }
    } catch (Exception e) {
      throw new TiClientInternalException("Can not cast Object to LocalDateTime: " + value, e);
    }
    long val = toPackedLong(in);
    codecObject.encodeNotNull(cdo, encodeType, val);
  }

  DateType(TiColumnInfo.InternalTypeHolder holder) {
    super(holder);
  }
}
