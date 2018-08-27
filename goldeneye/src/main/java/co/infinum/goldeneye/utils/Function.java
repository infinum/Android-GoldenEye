package co.infinum.goldeneye.utils;

public interface Function<In, Out> {
    Out invoke(In arg);
}
