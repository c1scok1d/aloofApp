package com.macinternetservices.aloofManager.room;

import android.database.Cursor;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.macinternetservices.aloofManager.model.GeoLoc;
import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unchecked", "deprecation"})
public final class GeoLocDao_Impl implements GeoLocDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<GeoLoc> __insertionAdapterOfGeoLoc;

  private final EntityDeletionOrUpdateAdapter<GeoLoc> __deletionAdapterOfGeoLoc;

  private final EntityDeletionOrUpdateAdapter<GeoLoc> __updateAdapterOfGeoLoc;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public GeoLocDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfGeoLoc = new EntityInsertionAdapter<GeoLoc>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `GeoLoc` (`id`,`name`,`isNotified`,`deviceid`,`status`,`latitude`,`longitude`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, GeoLoc value) {
        stmt.bindLong(1, value.getId());
        if (value.getName() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getName());
        }
        final Integer _tmp;
        _tmp = value.getNotified() == null ? null : (value.getNotified() ? 1 : 0);
        if (_tmp == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindLong(3, _tmp);
        }
        if (value.getDeviceid() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getDeviceid());
        }
        if (value.getStatus() == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindString(5, value.getStatus());
        }
        stmt.bindDouble(6, value.getLatitude());
        stmt.bindDouble(7, value.getLongitude());
      }
    };
    this.__deletionAdapterOfGeoLoc = new EntityDeletionOrUpdateAdapter<GeoLoc>(__db) {
      @Override
      public String createQuery() {
        return "DELETE FROM `GeoLoc` WHERE `id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, GeoLoc value) {
        stmt.bindLong(1, value.getId());
      }
    };
    this.__updateAdapterOfGeoLoc = new EntityDeletionOrUpdateAdapter<GeoLoc>(__db) {
      @Override
      public String createQuery() {
        return "UPDATE OR ABORT `GeoLoc` SET `id` = ?,`name` = ?,`isNotified` = ?,`deviceid` = ?,`status` = ?,`latitude` = ?,`longitude` = ? WHERE `id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, GeoLoc value) {
        stmt.bindLong(1, value.getId());
        if (value.getName() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getName());
        }
        final Integer _tmp;
        _tmp = value.getNotified() == null ? null : (value.getNotified() ? 1 : 0);
        if (_tmp == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindLong(3, _tmp);
        }
        if (value.getDeviceid() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getDeviceid());
        }
        if (value.getStatus() == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindString(5, value.getStatus());
        }
        stmt.bindDouble(6, value.getLatitude());
        stmt.bindDouble(7, value.getLongitude());
        stmt.bindLong(8, value.getId());
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "DELETE FROM GeoLoc";
        return _query;
      }
    };
  }

  @Override
  public void insert(final GeoLoc data) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfGeoLoc.insert(data);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final GeoLoc data) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfGeoLoc.handle(data);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final GeoLoc data) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfGeoLoc.handle(data);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteAll() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
    __db.beginTransaction();
    try {
      _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
      __preparedStmtOfDeleteAll.release(_stmt);
    }
  }

  @Override
  public List<GeoLoc> getAll() {
    final String _sql = "SELECT * FROM GeoLoc";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfIsNotified = CursorUtil.getColumnIndexOrThrow(_cursor, "isNotified");
      final int _cursorIndexOfDeviceid = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceid");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
      final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
      final List<GeoLoc> _result = new ArrayList<GeoLoc>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final GeoLoc _item;
        _item = new GeoLoc();
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _item.setId(_tmpId);
        final String _tmpName;
        _tmpName = _cursor.getString(_cursorIndexOfName);
        _item.setName(_tmpName);
        final Boolean _tmpIsNotified;
        final Integer _tmp;
        if (_cursor.isNull(_cursorIndexOfIsNotified)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getInt(_cursorIndexOfIsNotified);
        }
        _tmpIsNotified = _tmp == null ? null : _tmp != 0;
        _item.setNotified(_tmpIsNotified);
        final String _tmpDeviceid;
        _tmpDeviceid = _cursor.getString(_cursorIndexOfDeviceid);
        _item.setDeviceid(_tmpDeviceid);
        final String _tmpStatus;
        _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
        _item.setStatus(_tmpStatus);
        final double _tmpLatitude;
        _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
        _item.setLatitude(_tmpLatitude);
        final double _tmpLongitude;
        _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
        _item.setLongitude(_tmpLongitude);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }
}
