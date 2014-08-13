package cofh.asm;

import cofh.core.CoFHProps;
import cofh.core.item.IEqualityOverrideItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.Comparator;
import java.util.List;

import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class HooksCore {

	// { Optifine hooks
	public static <T extends Object> void sort(T[] list, int start, int toIndex, Comparator<T> cmp) {

		if (CoFHProps.enableRenderSorting) {
			quickSortArray(list, start, toIndex - 1, 0, cmp);
		}
	}

	public static <T extends Object> void sort(List<T> list, int start, int toIndex, Comparator<T> cmp) {

		if (CoFHProps.enableRenderSorting) {
			quickSortList(list, start, toIndex - 1, 0, cmp);
		}
	}

	// }

	// { Vanilla hooks
	public static <T extends Object> void sort(T[] list, Comparator<T> cmp) {

		if (CoFHProps.enableRenderSorting) {
			quickSortArray(list, 0, list.length - 1, 0, cmp);
		}
	}

	public static <T extends Object> void sort(List<T> list, Comparator<T> cmp) {

		if (CoFHProps.enableRenderSorting) {
			quickSortList(list, 0, list.size() - 1, 0, cmp);
		}
	}

	public static boolean areItemsEqualHook(ItemStack held, ItemStack lastHeld) {

		if (held.getItem() != lastHeld.getItem()) {
			return false;
		}
		Item item = held.getItem();
		if (item instanceof IEqualityOverrideItem && ((IEqualityOverrideItem) item).isLastHeldItemEqual(held, lastHeld)) {
			return true;
		}
		if (held.isItemStackDamageable() && held.getItemDamage() != lastHeld.getItemDamage()) {
			return false;
		}

		return ItemStack.areItemStackTagsEqual(held, lastHeld);
	}

	@SideOnly(Side.CLIENT)
	public static void tickTextures(ITickable obj) {

		if (CoFHProps.enableAnimatedTexutres) {
			obj.tick();
		}
	}

	// }

	private static <T> void quickSortList(List<T> input, int left, int right, int d, Comparator<T> cmp) {

		if (left >= right) {
			return;
		}
		int j = right, i = left;
		int size = right - left;
		T pivotPoint = input.get(((right >>> 1) + (left >>> 1))), t;
		do {
			if (size <= 7) {
				pivotPoint = input.get(left);
				do {
					do {
						++left;
						if (cmp.compare(input.get(left), pivotPoint) < 0) {
							pivotPoint = input.get(left);
							do { // this section can be improved.
								input.set(left--, t = input.get(left));
							} while (left > i && cmp.compare(pivotPoint, t) < 0);
							input.set(left, pivotPoint);
						}
					} while (left < right);
					++i;
					left = i;
					pivotPoint = input.get(left);
				} while (i < right);
				return;
			}
			while (left < right) {
				while (right > left & cmp.compare(input.get(right), pivotPoint) > 0) {
					--right;
				}
				while (left < right & cmp.compare(input.get(left), pivotPoint) < 0) {
					++left;
				}
				if (left < right) {
					t = input.get(left);
					input.set(left, input.get(right));
					input.set(right, t);
					++left;
					--right;
				}
			}
			if (right > 0) {
				if (left == right) {
					if (cmp.compare(input.get(left), pivotPoint) < 0) {
						++left;
					} else if (cmp.compare(input.get(right), pivotPoint) > 0) {
						--right;
					}
				}
				if (i < right) {
					quickSortList(input, i, right, d + 1, cmp);
				}
			}
			left |= (left == 0) & (right == 0) ? 1 : 0;
			if (j <= left) {
				return;
			}
			i = left;
			right = j;
			pivotPoint = input.get(((right >>> 1) + (left >>> 1)));
			size = right - left;
			++d;
		} while (true);
	}

	private static <T> void quickSortArray(T[] input, int left, int right, int d, Comparator<T> cmp) {

		if (left >= right) {
			return;
		}
		int j = right, i = left;
		int size = right - left;
		T pivotPoint = input[((right >>> 1) + (left >>> 1))], t;
		do {
			if (size <= 7) {
				pivotPoint = input[left];
				do {
					do {
						++left;
						if (cmp.compare(input[left], pivotPoint) < 0) {
							pivotPoint = input[left];
							do { // this section can be improved.
								input[left--] = t = input[left];
							} while (left > i && cmp.compare(pivotPoint, t) < 0);
							input[left] = pivotPoint;
						}
					} while (left < right);
					++i;
					left = i;
					pivotPoint = input[left];
				} while (i < right);
				return;
			}
			while (left < right) {
				while (right > left & cmp.compare(input[right], pivotPoint) > 0) {
					--right;
				}
				while (left < right & cmp.compare(input[left], pivotPoint) < 0) {
					++left;
				}
				if (left < right) {
					t = input[left];
					input[left] = input[right];
					input[right] = t;
					++left;
					--right;
				}
			}
			if (right > 0) {
				if (left == right) {
					if (cmp.compare(input[left], pivotPoint) < 0) {
						++left;
					} else if (cmp.compare(input[right], pivotPoint) > 0) {
						--right;
					}
				}
				if (i < right) {
					quickSortArray(input, i, right, d + 1, cmp);
				}
			}
			left |= (left == 0) & (right == 0) ? 1 : 0;
			if (j <= left) {
				return;
			}
			i = left;
			right = j;
			pivotPoint = input[((right >>> 1) + (left >>> 1))];
			size = right - left;
			++d;
		} while (true);
	}

}
