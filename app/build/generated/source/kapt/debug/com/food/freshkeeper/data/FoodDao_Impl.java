package com.food.freshkeeper.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class FoodDao_Impl implements FoodDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<FoodItem> __insertionAdapterOfFoodItem;

  private final EntityDeletionOrUpdateAdapter<FoodItem> __deletionAdapterOfFoodItem;

  private final EntityDeletionOrUpdateAdapter<FoodItem> __updateAdapterOfFoodItem;

  private final SharedSQLiteStatement __preparedStmtOfMoveToTrash;

  private final SharedSQLiteStatement __preparedStmtOfRestoreFromTrash;

  private final SharedSQLiteStatement __preparedStmtOfRestoreAllTrash;

  private final SharedSQLiteStatement __preparedStmtOfEmptyTrash;

  private final SharedSQLiteStatement __preparedStmtOfDeletePermanently;

  private final SharedSQLiteStatement __preparedStmtOfClearConsumed;

  private final SharedSQLiteStatement __preparedStmtOfClearExpired;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  public FoodDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfFoodItem = new EntityInsertionAdapter<FoodItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `food_items` (`id`,`name`,`category`,`iconEmoji`,`location`,`productionDateMs`,`shelfLifeDays`,`expiryDateMs`,`quantity`,`notes`,`imageUri`,`isConsumed`,`consumedAtMs`,`isDeleted`,`deletedAtMs`,`reminderDaysBefore`,`createdAtMs`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FoodItem entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
        if (entity.getCategory() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getCategory());
        }
        if (entity.getIconEmoji() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getIconEmoji());
        }
        if (entity.getLocation() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getLocation());
        }
        statement.bindLong(6, entity.getProductionDateMs());
        statement.bindLong(7, entity.getShelfLifeDays());
        statement.bindLong(8, entity.getExpiryDateMs());
        if (entity.getQuantity() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getQuantity());
        }
        if (entity.getNotes() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getNotes());
        }
        if (entity.getImageUri() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getImageUri());
        }
        final int _tmp = entity.isConsumed() ? 1 : 0;
        statement.bindLong(12, _tmp);
        if (entity.getConsumedAtMs() == null) {
          statement.bindNull(13);
        } else {
          statement.bindLong(13, entity.getConsumedAtMs());
        }
        final int _tmp_1 = entity.isDeleted() ? 1 : 0;
        statement.bindLong(14, _tmp_1);
        if (entity.getDeletedAtMs() == null) {
          statement.bindNull(15);
        } else {
          statement.bindLong(15, entity.getDeletedAtMs());
        }
        statement.bindLong(16, entity.getReminderDaysBefore());
        statement.bindLong(17, entity.getCreatedAtMs());
      }
    };
    this.__deletionAdapterOfFoodItem = new EntityDeletionOrUpdateAdapter<FoodItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `food_items` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FoodItem entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfFoodItem = new EntityDeletionOrUpdateAdapter<FoodItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `food_items` SET `id` = ?,`name` = ?,`category` = ?,`iconEmoji` = ?,`location` = ?,`productionDateMs` = ?,`shelfLifeDays` = ?,`expiryDateMs` = ?,`quantity` = ?,`notes` = ?,`imageUri` = ?,`isConsumed` = ?,`consumedAtMs` = ?,`isDeleted` = ?,`deletedAtMs` = ?,`reminderDaysBefore` = ?,`createdAtMs` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FoodItem entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
        if (entity.getCategory() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getCategory());
        }
        if (entity.getIconEmoji() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getIconEmoji());
        }
        if (entity.getLocation() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getLocation());
        }
        statement.bindLong(6, entity.getProductionDateMs());
        statement.bindLong(7, entity.getShelfLifeDays());
        statement.bindLong(8, entity.getExpiryDateMs());
        if (entity.getQuantity() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getQuantity());
        }
        if (entity.getNotes() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getNotes());
        }
        if (entity.getImageUri() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getImageUri());
        }
        final int _tmp = entity.isConsumed() ? 1 : 0;
        statement.bindLong(12, _tmp);
        if (entity.getConsumedAtMs() == null) {
          statement.bindNull(13);
        } else {
          statement.bindLong(13, entity.getConsumedAtMs());
        }
        final int _tmp_1 = entity.isDeleted() ? 1 : 0;
        statement.bindLong(14, _tmp_1);
        if (entity.getDeletedAtMs() == null) {
          statement.bindNull(15);
        } else {
          statement.bindLong(15, entity.getDeletedAtMs());
        }
        statement.bindLong(16, entity.getReminderDaysBefore());
        statement.bindLong(17, entity.getCreatedAtMs());
        statement.bindLong(18, entity.getId());
      }
    };
    this.__preparedStmtOfMoveToTrash = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE food_items SET isDeleted = 1, deletedAtMs = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfRestoreFromTrash = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE food_items SET isDeleted = 0, deletedAtMs = NULL WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfRestoreAllTrash = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE food_items SET isDeleted = 0, deletedAtMs = NULL WHERE isDeleted = 1";
        return _query;
      }
    };
    this.__preparedStmtOfEmptyTrash = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM food_items WHERE isDeleted = 1";
        return _query;
      }
    };
    this.__preparedStmtOfDeletePermanently = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM food_items WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearConsumed = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE food_items SET isDeleted = 1, deletedAtMs = ? WHERE isConsumed = 1 AND isDeleted = 0";
        return _query;
      }
    };
    this.__preparedStmtOfClearExpired = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE food_items SET isDeleted = 1, deletedAtMs = ? WHERE isConsumed = 0 AND isDeleted = 0 AND expiryDateMs < ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM food_items";
        return _query;
      }
    };
  }

  @Override
  public Object insertFood(final FoodItem food, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfFoodItem.insertAndReturnId(food);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAll(final List<FoodItem> foods,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfFoodItem.insert(foods);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteFood(final FoodItem food, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfFoodItem.handle(food);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateFood(final FoodItem food, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfFoodItem.handle(food);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object moveToTrash(final long id, final long deletedAtMs,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMoveToTrash.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, deletedAtMs);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfMoveToTrash.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object restoreFromTrash(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfRestoreFromTrash.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfRestoreFromTrash.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object restoreAllTrash(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfRestoreAllTrash.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfRestoreAllTrash.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object emptyTrash(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfEmptyTrash.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfEmptyTrash.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deletePermanently(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeletePermanently.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeletePermanently.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearConsumed(final long nowMs, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearConsumed.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, nowMs);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearConsumed.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearExpired(final long nowMs, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearExpired.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, nowMs);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, nowMs);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearExpired.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<FoodItem>> getAllFoods() {
    final String _sql = "SELECT * FROM food_items WHERE isDeleted = 0 ORDER BY expiryDateMs ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"food_items"}, new Callable<List<FoodItem>>() {
      @Override
      @NonNull
      public List<FoodItem> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfIconEmoji = CursorUtil.getColumnIndexOrThrow(_cursor, "iconEmoji");
          final int _cursorIndexOfLocation = CursorUtil.getColumnIndexOrThrow(_cursor, "location");
          final int _cursorIndexOfProductionDateMs = CursorUtil.getColumnIndexOrThrow(_cursor, "productionDateMs");
          final int _cursorIndexOfShelfLifeDays = CursorUtil.getColumnIndexOrThrow(_cursor, "shelfLifeDays");
          final int _cursorIndexOfExpiryDateMs = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryDateMs");
          final int _cursorIndexOfQuantity = CursorUtil.getColumnIndexOrThrow(_cursor, "quantity");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUri");
          final int _cursorIndexOfIsConsumed = CursorUtil.getColumnIndexOrThrow(_cursor, "isConsumed");
          final int _cursorIndexOfConsumedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "consumedAtMs");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAtMs");
          final int _cursorIndexOfReminderDaysBefore = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderDaysBefore");
          final int _cursorIndexOfCreatedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtMs");
          final List<FoodItem> _result = new ArrayList<FoodItem>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FoodItem _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final String _tmpIconEmoji;
            if (_cursor.isNull(_cursorIndexOfIconEmoji)) {
              _tmpIconEmoji = null;
            } else {
              _tmpIconEmoji = _cursor.getString(_cursorIndexOfIconEmoji);
            }
            final String _tmpLocation;
            if (_cursor.isNull(_cursorIndexOfLocation)) {
              _tmpLocation = null;
            } else {
              _tmpLocation = _cursor.getString(_cursorIndexOfLocation);
            }
            final long _tmpProductionDateMs;
            _tmpProductionDateMs = _cursor.getLong(_cursorIndexOfProductionDateMs);
            final int _tmpShelfLifeDays;
            _tmpShelfLifeDays = _cursor.getInt(_cursorIndexOfShelfLifeDays);
            final long _tmpExpiryDateMs;
            _tmpExpiryDateMs = _cursor.getLong(_cursorIndexOfExpiryDateMs);
            final String _tmpQuantity;
            if (_cursor.isNull(_cursorIndexOfQuantity)) {
              _tmpQuantity = null;
            } else {
              _tmpQuantity = _cursor.getString(_cursorIndexOfQuantity);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final String _tmpImageUri;
            if (_cursor.isNull(_cursorIndexOfImageUri)) {
              _tmpImageUri = null;
            } else {
              _tmpImageUri = _cursor.getString(_cursorIndexOfImageUri);
            }
            final boolean _tmpIsConsumed;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsConsumed);
            _tmpIsConsumed = _tmp != 0;
            final Long _tmpConsumedAtMs;
            if (_cursor.isNull(_cursorIndexOfConsumedAtMs)) {
              _tmpConsumedAtMs = null;
            } else {
              _tmpConsumedAtMs = _cursor.getLong(_cursorIndexOfConsumedAtMs);
            }
            final boolean _tmpIsDeleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_1 != 0;
            final Long _tmpDeletedAtMs;
            if (_cursor.isNull(_cursorIndexOfDeletedAtMs)) {
              _tmpDeletedAtMs = null;
            } else {
              _tmpDeletedAtMs = _cursor.getLong(_cursorIndexOfDeletedAtMs);
            }
            final int _tmpReminderDaysBefore;
            _tmpReminderDaysBefore = _cursor.getInt(_cursorIndexOfReminderDaysBefore);
            final long _tmpCreatedAtMs;
            _tmpCreatedAtMs = _cursor.getLong(_cursorIndexOfCreatedAtMs);
            _item = new FoodItem(_tmpId,_tmpName,_tmpCategory,_tmpIconEmoji,_tmpLocation,_tmpProductionDateMs,_tmpShelfLifeDays,_tmpExpiryDateMs,_tmpQuantity,_tmpNotes,_tmpImageUri,_tmpIsConsumed,_tmpConsumedAtMs,_tmpIsDeleted,_tmpDeletedAtMs,_tmpReminderDaysBefore,_tmpCreatedAtMs);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<FoodItem>> getActiveFoods() {
    final String _sql = "SELECT * FROM food_items WHERE isDeleted = 0 AND isConsumed = 0 ORDER BY expiryDateMs ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"food_items"}, new Callable<List<FoodItem>>() {
      @Override
      @NonNull
      public List<FoodItem> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfIconEmoji = CursorUtil.getColumnIndexOrThrow(_cursor, "iconEmoji");
          final int _cursorIndexOfLocation = CursorUtil.getColumnIndexOrThrow(_cursor, "location");
          final int _cursorIndexOfProductionDateMs = CursorUtil.getColumnIndexOrThrow(_cursor, "productionDateMs");
          final int _cursorIndexOfShelfLifeDays = CursorUtil.getColumnIndexOrThrow(_cursor, "shelfLifeDays");
          final int _cursorIndexOfExpiryDateMs = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryDateMs");
          final int _cursorIndexOfQuantity = CursorUtil.getColumnIndexOrThrow(_cursor, "quantity");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUri");
          final int _cursorIndexOfIsConsumed = CursorUtil.getColumnIndexOrThrow(_cursor, "isConsumed");
          final int _cursorIndexOfConsumedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "consumedAtMs");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAtMs");
          final int _cursorIndexOfReminderDaysBefore = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderDaysBefore");
          final int _cursorIndexOfCreatedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtMs");
          final List<FoodItem> _result = new ArrayList<FoodItem>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FoodItem _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final String _tmpIconEmoji;
            if (_cursor.isNull(_cursorIndexOfIconEmoji)) {
              _tmpIconEmoji = null;
            } else {
              _tmpIconEmoji = _cursor.getString(_cursorIndexOfIconEmoji);
            }
            final String _tmpLocation;
            if (_cursor.isNull(_cursorIndexOfLocation)) {
              _tmpLocation = null;
            } else {
              _tmpLocation = _cursor.getString(_cursorIndexOfLocation);
            }
            final long _tmpProductionDateMs;
            _tmpProductionDateMs = _cursor.getLong(_cursorIndexOfProductionDateMs);
            final int _tmpShelfLifeDays;
            _tmpShelfLifeDays = _cursor.getInt(_cursorIndexOfShelfLifeDays);
            final long _tmpExpiryDateMs;
            _tmpExpiryDateMs = _cursor.getLong(_cursorIndexOfExpiryDateMs);
            final String _tmpQuantity;
            if (_cursor.isNull(_cursorIndexOfQuantity)) {
              _tmpQuantity = null;
            } else {
              _tmpQuantity = _cursor.getString(_cursorIndexOfQuantity);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final String _tmpImageUri;
            if (_cursor.isNull(_cursorIndexOfImageUri)) {
              _tmpImageUri = null;
            } else {
              _tmpImageUri = _cursor.getString(_cursorIndexOfImageUri);
            }
            final boolean _tmpIsConsumed;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsConsumed);
            _tmpIsConsumed = _tmp != 0;
            final Long _tmpConsumedAtMs;
            if (_cursor.isNull(_cursorIndexOfConsumedAtMs)) {
              _tmpConsumedAtMs = null;
            } else {
              _tmpConsumedAtMs = _cursor.getLong(_cursorIndexOfConsumedAtMs);
            }
            final boolean _tmpIsDeleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_1 != 0;
            final Long _tmpDeletedAtMs;
            if (_cursor.isNull(_cursorIndexOfDeletedAtMs)) {
              _tmpDeletedAtMs = null;
            } else {
              _tmpDeletedAtMs = _cursor.getLong(_cursorIndexOfDeletedAtMs);
            }
            final int _tmpReminderDaysBefore;
            _tmpReminderDaysBefore = _cursor.getInt(_cursorIndexOfReminderDaysBefore);
            final long _tmpCreatedAtMs;
            _tmpCreatedAtMs = _cursor.getLong(_cursorIndexOfCreatedAtMs);
            _item = new FoodItem(_tmpId,_tmpName,_tmpCategory,_tmpIconEmoji,_tmpLocation,_tmpProductionDateMs,_tmpShelfLifeDays,_tmpExpiryDateMs,_tmpQuantity,_tmpNotes,_tmpImageUri,_tmpIsConsumed,_tmpConsumedAtMs,_tmpIsDeleted,_tmpDeletedAtMs,_tmpReminderDaysBefore,_tmpCreatedAtMs);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<FoodItem>> getConsumedFoods() {
    final String _sql = "SELECT * FROM food_items WHERE isDeleted = 0 AND isConsumed = 1 ORDER BY consumedAtMs DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"food_items"}, new Callable<List<FoodItem>>() {
      @Override
      @NonNull
      public List<FoodItem> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfIconEmoji = CursorUtil.getColumnIndexOrThrow(_cursor, "iconEmoji");
          final int _cursorIndexOfLocation = CursorUtil.getColumnIndexOrThrow(_cursor, "location");
          final int _cursorIndexOfProductionDateMs = CursorUtil.getColumnIndexOrThrow(_cursor, "productionDateMs");
          final int _cursorIndexOfShelfLifeDays = CursorUtil.getColumnIndexOrThrow(_cursor, "shelfLifeDays");
          final int _cursorIndexOfExpiryDateMs = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryDateMs");
          final int _cursorIndexOfQuantity = CursorUtil.getColumnIndexOrThrow(_cursor, "quantity");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUri");
          final int _cursorIndexOfIsConsumed = CursorUtil.getColumnIndexOrThrow(_cursor, "isConsumed");
          final int _cursorIndexOfConsumedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "consumedAtMs");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAtMs");
          final int _cursorIndexOfReminderDaysBefore = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderDaysBefore");
          final int _cursorIndexOfCreatedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtMs");
          final List<FoodItem> _result = new ArrayList<FoodItem>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FoodItem _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final String _tmpIconEmoji;
            if (_cursor.isNull(_cursorIndexOfIconEmoji)) {
              _tmpIconEmoji = null;
            } else {
              _tmpIconEmoji = _cursor.getString(_cursorIndexOfIconEmoji);
            }
            final String _tmpLocation;
            if (_cursor.isNull(_cursorIndexOfLocation)) {
              _tmpLocation = null;
            } else {
              _tmpLocation = _cursor.getString(_cursorIndexOfLocation);
            }
            final long _tmpProductionDateMs;
            _tmpProductionDateMs = _cursor.getLong(_cursorIndexOfProductionDateMs);
            final int _tmpShelfLifeDays;
            _tmpShelfLifeDays = _cursor.getInt(_cursorIndexOfShelfLifeDays);
            final long _tmpExpiryDateMs;
            _tmpExpiryDateMs = _cursor.getLong(_cursorIndexOfExpiryDateMs);
            final String _tmpQuantity;
            if (_cursor.isNull(_cursorIndexOfQuantity)) {
              _tmpQuantity = null;
            } else {
              _tmpQuantity = _cursor.getString(_cursorIndexOfQuantity);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final String _tmpImageUri;
            if (_cursor.isNull(_cursorIndexOfImageUri)) {
              _tmpImageUri = null;
            } else {
              _tmpImageUri = _cursor.getString(_cursorIndexOfImageUri);
            }
            final boolean _tmpIsConsumed;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsConsumed);
            _tmpIsConsumed = _tmp != 0;
            final Long _tmpConsumedAtMs;
            if (_cursor.isNull(_cursorIndexOfConsumedAtMs)) {
              _tmpConsumedAtMs = null;
            } else {
              _tmpConsumedAtMs = _cursor.getLong(_cursorIndexOfConsumedAtMs);
            }
            final boolean _tmpIsDeleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_1 != 0;
            final Long _tmpDeletedAtMs;
            if (_cursor.isNull(_cursorIndexOfDeletedAtMs)) {
              _tmpDeletedAtMs = null;
            } else {
              _tmpDeletedAtMs = _cursor.getLong(_cursorIndexOfDeletedAtMs);
            }
            final int _tmpReminderDaysBefore;
            _tmpReminderDaysBefore = _cursor.getInt(_cursorIndexOfReminderDaysBefore);
            final long _tmpCreatedAtMs;
            _tmpCreatedAtMs = _cursor.getLong(_cursorIndexOfCreatedAtMs);
            _item = new FoodItem(_tmpId,_tmpName,_tmpCategory,_tmpIconEmoji,_tmpLocation,_tmpProductionDateMs,_tmpShelfLifeDays,_tmpExpiryDateMs,_tmpQuantity,_tmpNotes,_tmpImageUri,_tmpIsConsumed,_tmpConsumedAtMs,_tmpIsDeleted,_tmpDeletedAtMs,_tmpReminderDaysBefore,_tmpCreatedAtMs);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<FoodItem>> getTrashFoods() {
    final String _sql = "SELECT * FROM food_items WHERE isDeleted = 1 ORDER BY deletedAtMs DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"food_items"}, new Callable<List<FoodItem>>() {
      @Override
      @NonNull
      public List<FoodItem> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfIconEmoji = CursorUtil.getColumnIndexOrThrow(_cursor, "iconEmoji");
          final int _cursorIndexOfLocation = CursorUtil.getColumnIndexOrThrow(_cursor, "location");
          final int _cursorIndexOfProductionDateMs = CursorUtil.getColumnIndexOrThrow(_cursor, "productionDateMs");
          final int _cursorIndexOfShelfLifeDays = CursorUtil.getColumnIndexOrThrow(_cursor, "shelfLifeDays");
          final int _cursorIndexOfExpiryDateMs = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryDateMs");
          final int _cursorIndexOfQuantity = CursorUtil.getColumnIndexOrThrow(_cursor, "quantity");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUri");
          final int _cursorIndexOfIsConsumed = CursorUtil.getColumnIndexOrThrow(_cursor, "isConsumed");
          final int _cursorIndexOfConsumedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "consumedAtMs");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAtMs");
          final int _cursorIndexOfReminderDaysBefore = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderDaysBefore");
          final int _cursorIndexOfCreatedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtMs");
          final List<FoodItem> _result = new ArrayList<FoodItem>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FoodItem _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final String _tmpIconEmoji;
            if (_cursor.isNull(_cursorIndexOfIconEmoji)) {
              _tmpIconEmoji = null;
            } else {
              _tmpIconEmoji = _cursor.getString(_cursorIndexOfIconEmoji);
            }
            final String _tmpLocation;
            if (_cursor.isNull(_cursorIndexOfLocation)) {
              _tmpLocation = null;
            } else {
              _tmpLocation = _cursor.getString(_cursorIndexOfLocation);
            }
            final long _tmpProductionDateMs;
            _tmpProductionDateMs = _cursor.getLong(_cursorIndexOfProductionDateMs);
            final int _tmpShelfLifeDays;
            _tmpShelfLifeDays = _cursor.getInt(_cursorIndexOfShelfLifeDays);
            final long _tmpExpiryDateMs;
            _tmpExpiryDateMs = _cursor.getLong(_cursorIndexOfExpiryDateMs);
            final String _tmpQuantity;
            if (_cursor.isNull(_cursorIndexOfQuantity)) {
              _tmpQuantity = null;
            } else {
              _tmpQuantity = _cursor.getString(_cursorIndexOfQuantity);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final String _tmpImageUri;
            if (_cursor.isNull(_cursorIndexOfImageUri)) {
              _tmpImageUri = null;
            } else {
              _tmpImageUri = _cursor.getString(_cursorIndexOfImageUri);
            }
            final boolean _tmpIsConsumed;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsConsumed);
            _tmpIsConsumed = _tmp != 0;
            final Long _tmpConsumedAtMs;
            if (_cursor.isNull(_cursorIndexOfConsumedAtMs)) {
              _tmpConsumedAtMs = null;
            } else {
              _tmpConsumedAtMs = _cursor.getLong(_cursorIndexOfConsumedAtMs);
            }
            final boolean _tmpIsDeleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_1 != 0;
            final Long _tmpDeletedAtMs;
            if (_cursor.isNull(_cursorIndexOfDeletedAtMs)) {
              _tmpDeletedAtMs = null;
            } else {
              _tmpDeletedAtMs = _cursor.getLong(_cursorIndexOfDeletedAtMs);
            }
            final int _tmpReminderDaysBefore;
            _tmpReminderDaysBefore = _cursor.getInt(_cursorIndexOfReminderDaysBefore);
            final long _tmpCreatedAtMs;
            _tmpCreatedAtMs = _cursor.getLong(_cursorIndexOfCreatedAtMs);
            _item = new FoodItem(_tmpId,_tmpName,_tmpCategory,_tmpIconEmoji,_tmpLocation,_tmpProductionDateMs,_tmpShelfLifeDays,_tmpExpiryDateMs,_tmpQuantity,_tmpNotes,_tmpImageUri,_tmpIsConsumed,_tmpConsumedAtMs,_tmpIsDeleted,_tmpDeletedAtMs,_tmpReminderDaysBefore,_tmpCreatedAtMs);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<FoodItem> getFoodById(final long id) {
    final String _sql = "SELECT * FROM food_items WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"food_items"}, new Callable<FoodItem>() {
      @Override
      @Nullable
      public FoodItem call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfIconEmoji = CursorUtil.getColumnIndexOrThrow(_cursor, "iconEmoji");
          final int _cursorIndexOfLocation = CursorUtil.getColumnIndexOrThrow(_cursor, "location");
          final int _cursorIndexOfProductionDateMs = CursorUtil.getColumnIndexOrThrow(_cursor, "productionDateMs");
          final int _cursorIndexOfShelfLifeDays = CursorUtil.getColumnIndexOrThrow(_cursor, "shelfLifeDays");
          final int _cursorIndexOfExpiryDateMs = CursorUtil.getColumnIndexOrThrow(_cursor, "expiryDateMs");
          final int _cursorIndexOfQuantity = CursorUtil.getColumnIndexOrThrow(_cursor, "quantity");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUri");
          final int _cursorIndexOfIsConsumed = CursorUtil.getColumnIndexOrThrow(_cursor, "isConsumed");
          final int _cursorIndexOfConsumedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "consumedAtMs");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAtMs");
          final int _cursorIndexOfReminderDaysBefore = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderDaysBefore");
          final int _cursorIndexOfCreatedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtMs");
          final FoodItem _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final String _tmpIconEmoji;
            if (_cursor.isNull(_cursorIndexOfIconEmoji)) {
              _tmpIconEmoji = null;
            } else {
              _tmpIconEmoji = _cursor.getString(_cursorIndexOfIconEmoji);
            }
            final String _tmpLocation;
            if (_cursor.isNull(_cursorIndexOfLocation)) {
              _tmpLocation = null;
            } else {
              _tmpLocation = _cursor.getString(_cursorIndexOfLocation);
            }
            final long _tmpProductionDateMs;
            _tmpProductionDateMs = _cursor.getLong(_cursorIndexOfProductionDateMs);
            final int _tmpShelfLifeDays;
            _tmpShelfLifeDays = _cursor.getInt(_cursorIndexOfShelfLifeDays);
            final long _tmpExpiryDateMs;
            _tmpExpiryDateMs = _cursor.getLong(_cursorIndexOfExpiryDateMs);
            final String _tmpQuantity;
            if (_cursor.isNull(_cursorIndexOfQuantity)) {
              _tmpQuantity = null;
            } else {
              _tmpQuantity = _cursor.getString(_cursorIndexOfQuantity);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final String _tmpImageUri;
            if (_cursor.isNull(_cursorIndexOfImageUri)) {
              _tmpImageUri = null;
            } else {
              _tmpImageUri = _cursor.getString(_cursorIndexOfImageUri);
            }
            final boolean _tmpIsConsumed;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsConsumed);
            _tmpIsConsumed = _tmp != 0;
            final Long _tmpConsumedAtMs;
            if (_cursor.isNull(_cursorIndexOfConsumedAtMs)) {
              _tmpConsumedAtMs = null;
            } else {
              _tmpConsumedAtMs = _cursor.getLong(_cursorIndexOfConsumedAtMs);
            }
            final boolean _tmpIsDeleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_1 != 0;
            final Long _tmpDeletedAtMs;
            if (_cursor.isNull(_cursorIndexOfDeletedAtMs)) {
              _tmpDeletedAtMs = null;
            } else {
              _tmpDeletedAtMs = _cursor.getLong(_cursorIndexOfDeletedAtMs);
            }
            final int _tmpReminderDaysBefore;
            _tmpReminderDaysBefore = _cursor.getInt(_cursorIndexOfReminderDaysBefore);
            final long _tmpCreatedAtMs;
            _tmpCreatedAtMs = _cursor.getLong(_cursorIndexOfCreatedAtMs);
            _result = new FoodItem(_tmpId,_tmpName,_tmpCategory,_tmpIconEmoji,_tmpLocation,_tmpProductionDateMs,_tmpShelfLifeDays,_tmpExpiryDateMs,_tmpQuantity,_tmpNotes,_tmpImageUri,_tmpIsConsumed,_tmpConsumedAtMs,_tmpIsDeleted,_tmpDeletedAtMs,_tmpReminderDaysBefore,_tmpCreatedAtMs);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM food_items WHERE isDeleted = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object moveToTrashBatch(final List<Long> ids, final long deletedAtMs,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("UPDATE food_items SET isDeleted = 1, deletedAtMs = ");
        _stringBuilder.append("?");
        _stringBuilder.append(" WHERE id IN (");
        final int _inputSize = ids.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, deletedAtMs);
        _argIndex = 2;
        for (Long _item : ids) {
          if (_item == null) {
            _stmt.bindNull(_argIndex);
          } else {
            _stmt.bindLong(_argIndex, _item);
          }
          _argIndex++;
        }
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
