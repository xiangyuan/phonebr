package com.bee.br.phone;

import java.util.ArrayList;


import android.content.ContentValues;
import android.net.Uri;

public class ContactHelper {
	public enum OP {
		insert, update, delete
	};

	public static class ItemWithOp<T extends Item> {
		T item;
		OP op;
		
		/** ����ר�� */
		T oldItem;	
		
		public ItemWithOp(OP op, T item) {
			this.op = op;
			this.item = item;
		}
	}
	
	public interface IItemCompare<T> {
		boolean compareKey(T pcItem, T phoneItem);
		boolean compareValue(T pcItem, T phoneItem);
	}
	
	public static <T extends Item> ArrayList<ItemWithOp<T>> diff(T[] pcItems, T[] phoneItems, IItemCompare<T> itemCompare) {
		if (phoneItems == null && pcItems == null)
			return null;

		if (phoneItems == null) {
			ArrayList<ItemWithOp<T>> list = new ArrayList<ItemWithOp<T>>();
			
			for (T item : pcItems)
				list.add(new ItemWithOp<T>(OP.insert, item));
			return list;
		}
		if (pcItems == null) {
			ArrayList<ItemWithOp<T>> list = new ArrayList<ItemWithOp<T>>();
			
			for (T item : phoneItems) 
				list.add(new ItemWithOp<T>(OP.delete, item));
			return list;
		}
		
		// ���鲻����༭�����Ըĳɶ���ģʽ
		ArrayList<T> pcList = new ArrayList<T>(pcItems.length);
		for(T item: pcItems)
			pcList.add(item);
		
		ArrayList<T> phoneList = new ArrayList<T>(phoneItems.length);
		for(T item: phoneItems)
			phoneList.add(item);

		return diffList(pcList, phoneList, itemCompare);
	}
	
//	/** ��Ҫ�Ǵ���һ����Ƴ��ֶ�ε���� */
//	private static <T extends Item> ArrayList<ItemWithOp<T>> diffListForId(ArrayList<T> pcList, ArrayList<T> phoneList, IItemCompare<T> itemCompare){
//		ArrayList<ItemWithOp<T>> list = new ArrayList<ItemWithOp<T>>();
//		
//		boolean isFind, sameName, sameValue;
//		String phoneItemId, pcItemId;
//		
//		while (phoneList.size() > 0) {
//			T phoneItem = phoneList.get(0);
//			isFind = false;
//			
//			phoneItemId = phoneItem.getId(); // is not null
//
//			for(int i=0; i< pcList.size(); i++){
//				T pcItem = pcList.get(i);
//				pcItemId = pcItem.getId();
//				
//				if (pcItemId == null) // new
//					continue;
//				
//				if (phoneItemId.equals(pcItemId)) {
//					isFind = true;
//					
//					sameName = itemCompare.compareKey(pcItem, phoneItem);
//					sameValue = itemCompare.compareValue(pcItem, phoneItem);
//					if (!sameName || !sameValue){
//						ItemWithOp<T> iop = new ItemWithOp<T>(OP.update, pcItem);						
//						iop.oldItem = phoneItem; 
//						
//						list.add(iop);
//					}
//					pcList.remove(i);
//					break;
//				}
//			}
//			if (!isFind)
//				list.add(new ItemWithOp<T>(OP.delete, phoneItem)); // û���ҵ�Ϊɾ��
//
//			phoneList.remove(0);
//		}
//		for(T item: pcList)
//			list.add(new ItemWithOp<T>(OP.insert, item)); // δ���ֵ�Ϊ����
//
//		return list;
//	}
	
	/** ��Ҫ�Ǵ���һ����Ƴ��ֶ�ε���� */
	private static <T extends Item> ArrayList<ItemWithOp<T>> diffList(ArrayList<T> pcList, ArrayList<T> phoneList, IItemCompare<T> itemCompare){
		ArrayList<ItemWithOp<T>> list = new ArrayList<ItemWithOp<T>>();
		
		boolean isFind, sameName, sameValue;
		
		while (phoneList.size() > 0) {
			T phoneItem = phoneList.get(0);
			isFind = false;
			
			for(int i=0; i< pcList.size(); i++){
				T pcItem = pcList.get(i);
				
				sameName = itemCompare.compareKey(pcItem, phoneItem);
				
				if (sameName){
					isFind = true;
					
					sameValue = itemCompare.compareValue(pcItem, phoneItem);
					if (!sameValue){
						ItemWithOp<T> iop = new ItemWithOp<T>(OP.update, pcItem);						
						iop.oldItem = phoneItem; 
						
						list.add(iop);
					}					
					pcList.remove(i);
					break;
				}
			}
			if (!isFind)
				list.add(new ItemWithOp<T>(OP.delete, phoneItem)); // û���ҵ�Ϊɾ��

			phoneList.remove(0);
		}
		for(T item: pcList)
			list.add(new ItemWithOp<T>(OP.insert, item)); // δ���ֵ�Ϊ����

		return list;
	}	
	
	public interface IContactItem<T> {
		Uri queryUri(long personId, T item);

		void updateTo(T item, ContentValues values, boolean clearValues);
		void insertTo(long rawContactId, T item, ContentValues values);

		Uri insert(long personId, T item, ContentValues values);
//		int bulkInsert(long personId, T[] items);

		int update(long personId, T item, T oldItem, ContentValues values);
		int delete(long personId, T item);
	}
		
	public static <T extends Item> int operateItems(long personId, IContactItem<T> contactItem,
			T[] pcItems, T[] phoneItems, IItemCompare<T> itemCompare) {
		ArrayList<ItemWithOp<T>> itemWithOp = diff(pcItems,
				phoneItems, itemCompare);
		int count = 0;
		if (itemWithOp != null) {
			ContentValues values = new ContentValues();

			for (ItemWithOp<T> itemOp : itemWithOp) {
				switch (itemOp.op) {
				case insert:
					Uri uri = contactItem.insert(personId, itemOp.item, values);
					if (uri != null)
						count++;
					break;
				case update:
					count += contactItem.update(personId, itemOp.item, itemOp.oldItem, values);
					break;
				case delete:
					count += contactItem.delete(personId, itemOp.item);
					break;
				}
			}
		}
		return count;
	}
}
